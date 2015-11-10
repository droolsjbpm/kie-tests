package org.kie.tests.wb.base;

import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.findTaskIdByProcessInstanceId;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runHumanTaskGroupIdTest;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runHumanTaskGroupVarAssignTest;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runReassignmentTaskTest;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runRemoteApiGroupAssignmentEngineeringTest;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runRemoteApiHumanTaskOwnTypeTest;
import static org.kie.tests.wb.base.methods.KieWbGeneralIntegrationTestMethods.runRuleTaskProcess;
import static org.kie.tests.wb.base.util.TestConstants.ARTIFACT_ID;
import static org.kie.tests.wb.base.util.TestConstants.GROUP_ID;
import static org.kie.tests.wb.base.util.TestConstants.HUMAN_TASK_VAR_PROCESS_ID;
import static org.kie.tests.wb.base.util.TestConstants.IMAGE_PROCESS_ID;
import static org.kie.tests.wb.base.util.TestConstants.MARY_USER;
import static org.kie.tests.wb.base.util.TestConstants.SINGLE_HUMAN_TASK_PROCESS_ID;
import static org.kie.tests.wb.base.util.TestConstants.TASK_CONTENT_PROCESS_ID;
import static org.kie.tests.wb.base.util.TestConstants.VERSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;

import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.process.audit.CommandBasedAuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.tests.MyBinaryType;
import org.kie.tests.MyType;
import org.kie.tests.wb.base.methods.KieWbJmsIntegrationTestMethods;
import org.kie.tests.wb.base.util.TestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(ProcessTest.class);

    public ProcessTest() {
        super(true, true, "org.jbpm.domain");
    }

    @Test
    public void showMyBinaryTypeXml() throws Exception {
        MyBinaryType type = new MyBinaryType("file-name", "random bytes".getBytes());
        Class<?> [] arr = {MyBinaryType.class};
        JaxbSerializationProvider jaxbProvider = ClientJaxbSerializationProvider.newInstance(Arrays.asList(arr));
        jaxbProvider.addJaxbClasses(MyBinaryType.class);
       System.out.println( jaxbProvider.serialize(type) );
    }

    @Test
    public void runRuleTaskProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/ruleTask.bpmn2", ResourceType.BPMN2);
        resources.put("repo/test/ruleTask.drl", ResourceType.DRL);
        RuntimeManager runtimeManager = createRuntimeManager(resources);
        KieSession ksession = runtimeManager.getRuntimeEngine(null).getKieSession();

        // test
        runRuleTaskProcess(ksession, new CommandBasedAuditLogService(ksession));
    }

    @Test
    public void runUserTaskContentProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/userTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        // test
        ProcessInstance procInst = ksession.startProcess(TASK_CONTENT_PROCESS_ID);
        long procInstId = procInst.getId();

        List<Long> taskIdList = taskService.getTasksByProcessInstanceId(procInstId);
        assertEquals(taskIdList.size(), 1);
        long taskId = taskIdList.get(0);

        Task task = taskService.getTaskById(taskId);
        TaskData taskData = task.getTaskData();
        long contentId = taskData.getDocumentContentId();
        Content content = taskService.getContentById(task.getTaskData().getDocumentContentId());
        assertNotNull("No content found!", content);
        assertTrue("Content is empty!", content.getContent().length > 0);
        Map<String, Object> contMap = (Map) ContentMarshallerHelper.unmarshall(content.getContent(), null);

        assertEquals("reviewer", contMap.get("GroupId"));
        assertEquals(3, contMap.keySet().size());
    }

    @Test
    public void runObjectParamProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/objectVariableProcess.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TestWih testWih = new TestWih();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testWih);

        Map<String, Object> params = new HashMap<String, Object>();
        String varId = "myobject";
        int varVal = 10;
        String txtVal = "test";
        MyType type = new MyType(txtVal, varVal);
        params.put(varId, type);
        ProcessInstance procInst = ksession.startProcess(TestConstants.OBJECT_VARIABLE_PROCESS_ID, params);
        long processInstanceId = procInst.getId();

        Map<String, Object> varMap = ((WorkflowProcessInstanceImpl) procInst).getVariables();
        assertNotNull("Null variable instance found.", varMap);
        for (Entry<String, Object> entry : varMap.entrySet()) {
            logger.debug(entry.getKey() + " (" + entry.getValue().getClass().getSimpleName() + ") " + entry.getValue());
        }

        List<VariableInstanceLog> viLogs = new JPAAuditLogService(getEmf()).findVariableInstancesByName(varId, false);
        assertTrue(viLogs.size() > 0);
        assertEquals(varId, viLogs.get(0).getVariableId());

        assertNotNull("Empty VariableInstanceLog list.", viLogs);
        assertEquals("VariableInstanceLog list size",  1, viLogs.size());
        VariableInstanceLog vil = viLogs.get(0);
        assertNotNull("Empty VariableInstanceLog instance.", vil);
        assertEquals("Process instance id", vil.getProcessInstanceId().longValue(), processInstanceId);
        assertEquals("Variable id", vil.getVariableId(), "myobject");
        assertEquals("Variable value", vil.getValue(), type.toString());


        ksession.getWorkItemManager().completeWorkItem(testWih.workItemList.poll().getId(), null);
        procInst = ksession.getProcessInstance(processInstanceId);
        assertNull(procInst);
    }

    private static class TestWih implements WorkItemHandler {

        Queue<WorkItem> workItemList = new LinkedList<WorkItem>();

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            workItemList.add(workItem);
        }

        @Override
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
            throw new IllegalStateException("Not allowed to abort workitem in test (workitem " + workItem.getName() + ")");
        }

    }

    @Test
    public void runHumanTaskProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/humanTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources)
                ;

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        // test
        ProcessInstance procInst = ksession.startProcess(TestConstants.HUMAN_TASK_PROCESS_ID);
        long procInstId = procInst.getId();

        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Ready);
        List<TaskSummary> taskSumList = taskService.getTasksByStatusByProcessInstanceId(procInstId, statuses, "en-UK");
        assertEquals("No tasks found for proc inst " + procInstId + " and status " + Status.Ready, taskSumList.size(), 1);
        TaskSummary taskSum = taskSumList.get(0);

        long taskId = taskSum.getId();
        assertNull(taskSum.getActualOwner());
    }

    @Test
    public void humanTaskGroupIdTest() throws Exception {
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/evaluation.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        runHumanTaskGroupIdTest(runtimeEngine, runtimeEngine, runtimeEngine);
    }

    @Test
    public void runGroupAssignmentEngineeringTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/groupAssignmentHumanTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        KieWbJmsIntegrationTestMethods jmsTests = new KieWbJmsIntegrationTestMethods("blah", false, false);
        jmsTests.remoteApiGroupAssignmentEngineering(runtimeEngine);
    }

    @Test
    public void runGroupAssignmentHumanTaskTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/groupAssignmentHumanTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        runRemoteApiGroupAssignmentEngineeringTest(runtimeEngine, runtimeEngine);
    }

    @Test
    public void runGroupAssignmentVarAssignTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/singleHumanTaskGroupAssignment.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        runHumanTaskGroupVarAssignTest(runtimeEngine, MARY_USER, "HR", new GetProcessIdsCommand());
    }

    @Test
    public void runHumanTaskWithOwnTypeTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/humanTaskWithOwnType.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        KModuleDeploymentUnit depUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        runRemoteApiHumanTaskOwnTypeTest(runtimeEngine, runtimeEngine.getAuditService() );
    }

    @Test
    @Ignore
    public void runClassPathProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/classpath/objectVariableProcess.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);

        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();

        Map<String, Object> params = new HashMap<String, Object>();
        String varId = "myobject";
        String text = UUID.randomUUID().toString();
        params.put(varId, new MyType(text, 10));
        ProcessInstance procInst = ksession.startProcess(TestConstants.CLASSPATH_OBJECT_PROCESS_ID, params);
        long processInstanceId = procInst.getId();

        Map<String, Object> varMap = ((WorkflowProcessInstanceImpl) procInst).getVariables();
        assertNotNull("Null variable instance found.", varMap);
        for (Entry<String, Object> entry : varMap.entrySet()) {
            logger.debug(entry.getKey() + " (" + entry.getValue().getClass().getSimpleName() + ") " + entry.getValue());
        }

        List<VariableInstanceLog> varLogs = new JPAAuditLogService(getEmf()).findVariableInstancesByName(varId, false);
        assertTrue(varLogs.size() > 0);
        assertEquals(varId, varLogs.get(0).getVariableId());

        TaskService taskService = runtimeEngine.getTaskService();
        List<Long> taskIds =  taskService.getTasksByProcessInstanceId(processInstanceId);
        long taskId = taskIds.get(0);
        Task task = taskService.getTaskById(taskId);
        taskService.start(taskIds.get(0), "Administrator");
        taskService.complete(taskIds.get(0), "Administrator", null);

        procInst = ksession.getProcessInstance(processInstanceId);
        assertTrue(procInst == null || procInst.getState() == ProcessInstance.STATE_COMPLETED );
    }

    @Test
    public void singleHumanTaskProcessTest() {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/singleHumanTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);;
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        // 1. start process (could do it this way or a via REST url or whatever..
        ProcessInstance processInstance = ksession.startProcess(SINGLE_HUMAN_TASK_PROCESS_ID);
        assertNotNull("Null ProcessInstance!", processInstance);
        long procInstId = processInstance.getId();
        logger.debug("Started process instance: " + processInstance + " " + procInstId);

        // 2. find task (without deployment id)
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(MARY_USER, "en-UK");
        long taskId = findTaskIdByProcessInstanceId(procInstId, tasks);
        logger.debug("Found task " + taskId);

        // 3. retrieve task and get deployment id from task
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId);
        String deploymentId = task.getTaskData().getDeploymentId();
        logger.debug("Got deployment id " + deploymentId );

        // 4. start task
        taskService.start(taskId, MARY_USER);

        // 5. complete task with TaskService instance that *has a deployment id*
        taskService.complete(taskId, MARY_USER, null);

        // 6. verify that process has completed
        processInstance = ksession.getProcessInstance(procInstId);
        assertTrue( "Process instance has not completed!", processInstance == null || processInstance.getState() == ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void urlHumanTaskTestTest() {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/humanTaskVar.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);;
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("userName", "John");
        ProcessInstance procInst = ksession.startProcess(HUMAN_TASK_VAR_PROCESS_ID, vars);
        long procInstId = procInst.getId();

        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        long taskId = taskIds.get(0);

        taskService.start(taskId, MARY_USER);

        Map<String, Object> results = new HashMap<String, Object>();
        String georgeVal = "George";
        results.put("outUserName", georgeVal);
        taskService.complete(taskId, MARY_USER, results);

        try {
            taskService.complete(taskId, MARY_USER, results);
            fail("Second task complete should have failed!");
        } catch( Exception e ) {

        }

        AuditService auditService = runtimeEngine.getAuditService();
        List<? extends org.kie.api.runtime.manager.audit.VariableInstanceLog> varLogs = auditService.findVariableInstances(procInstId);
        boolean georgeFound = false;
        for( org.kie.api.runtime.manager.audit.VariableInstanceLog varLog : varLogs ) {
            if( "userName".equals(varLog.getVariableId()) && georgeVal.equals(varLog.getValue()) ) {
                georgeFound = true;
            }
        }
        assertTrue("'userName' var with value '" + georgeVal + "' not found!", georgeFound);

        Map<String, Object> contentMap = taskService.getTaskContent(taskId);
        String groupId = null;
        for( Entry<String, Object> entry : contentMap.entrySet() ) {
            if( entry.getKey().equals("GroupId") ) {
                groupId = new String((String) entry.getValue());
                break;
            }
        }
        assertEquals("reviewer", groupId);

        ksession.signalEvent("MySignal", "MySignal", procInstId);

        procInst = ksession.getProcessInstance(procInstId);
        assertTrue( "State: "  +  (procInst == null ? 2 : procInst.getState()), procInst == null || procInst.getState() == ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void runImageProcessTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/processImage.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);
        KieSession ksession = runtimeManager.getRuntimeEngine(null).getKieSession();
        TaskService taskService = runtimeManager.getRuntimeEngine(null).getTaskService();

        // test
        ProcessInstance procInst = ksession.startProcess(IMAGE_PROCESS_ID);
        assertNotNull( "Null process instance", procInst );
        assertEquals( "Process instance state", ProcessInstance.STATE_ACTIVE, procInst.getState() );
        long procInstId = procInst.getId();

        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No task ids found", taskIds.isEmpty() );
        assertEquals( "Task ids for this process instance", 1,  taskIds.size() );

        long taskId = taskIds.get(0);

        taskService.start(taskId, MARY_USER);
        taskService.complete(taskId, MARY_USER, null);

        procInst = ksession.getProcessInstance(procInstId);
        assertTrue( "Process instance has not completed!", procInst == null || procInst.getState() == ProcessInstance.STATE_COMPLETED );
    }

    @Test
    public void runReassignmentTest() throws Exception {
        // setup
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        resources.put("repo/test/reassignmentTask.bpmn2", ResourceType.BPMN2);
        RuntimeManager runtimeManager = createRuntimeManager(resources);
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);

        runReassignmentTaskTest(runtimeEngine);
    }


}