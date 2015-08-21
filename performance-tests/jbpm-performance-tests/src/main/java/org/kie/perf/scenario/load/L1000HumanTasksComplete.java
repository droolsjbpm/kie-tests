package org.kie.perf.scenario.load;

import java.util.List;

import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskService;
import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.annotation.KPKLimit;
import org.kie.perf.jbpm.JBPMController;
import org.kie.perf.jbpm.constant.UserStorage;
import org.kie.perf.scenario.IPerfTest;
import org.kie.perf.scenario.PrepareEngine;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

@KPKLimit(1000)
public class L1000HumanTasksComplete implements IPerfTest {

    private JBPMController jc;

    private TaskService taskService;

    private Meter taskCompleted;

    private List<Long> taskIds;

    @Override
    public void init() {

        jc = JBPMController.getInstance();

        jc.addTaskEventListener(new DefaultTaskEventListener() {
            @Override
            public void afterTaskCompletedEvent(TaskEvent event) {
                taskCompleted.mark();
            }
        });

        jc.createRuntimeManager();

        taskService = jc.getRuntimeEngine().getTaskService();

        taskIds = PrepareEngine.createNewTasks(true, 1000, taskService);
    }

    @Override
    public void initMetrics() {
        taskId = 0;
        MetricRegistry metrics = SharedMetricRegistry.getInstance();
        taskCompleted = metrics.meter(MetricRegistry.name(L1000HumanTasksComplete.class, "scenario.task.completed"));
    }

    static int taskId = 0;

    @Override
    public void execute() {
        Long tid = taskIds.get(taskId);
        taskService.complete(tid, UserStorage.PerfUser.getUserId(), null);
        taskId++;
    }

    @Override
    public void close() {
        jc.tearDown();
    }

}
