package org.kie.remote.tests.base.util;

import static org.kie.remote.tests.base.RestUtil.failAndLog;

import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

public class JsonResponseHandler<T,P> extends AbstractResponseHandler<T, P> {

    public JsonResponseHandler(int status, Class<T>... returnTypes) { 
        super(ContentType.APPLICATION_JSON, status, returnTypes);
    }
    
    public JsonResponseHandler(Class<T>... returnTypes) { 
        super(ContentType.APPLICATION_JSON, returnTypes);
    }
    
    private final ObjectMapper om = new ObjectMapper();
   
    @Override
    protected T deserialize(String content) {
        if( parameterType == null ) { 
            try {
                T obj =  om.readValue(content, returnType);
                if( logger.isTraceEnabled() ) { 
                   Object prettyPrintObj = om.readValue(content, Object.class);
                   String prettyContent = om.writerWithDefaultPrettyPrinter().writeValueAsString(prettyPrintObj);
                   logger.trace("JSON < |\n{}", prettyContent );
                }
                return obj;
            } catch( Exception e ) {
               failAndLog(returnType.getSimpleName() + " deserialization failed", e); 
            } 
        } else { 
            JavaType genericsType = om.getTypeFactory().constructParametricType(this.returnType, this.parameterType);
            try { 
                return (T) om.readValue(content, genericsType);
            } catch( Exception e ) {
               failAndLog(returnType.getSimpleName() + "<" + parameterType.getSimpleName() + ">" + " deserialization failed", e); 
            } 
        }
        
        // never happens
        return null;
    }

    @Override
    public String serialize( Object entity ) {
        String out = null;
        try {
            out = om.writeValueAsString(entity);
            if( logger.isTraceEnabled() ) { 
                logger.trace("JSON > |\n{} ", om.writerWithDefaultPrettyPrinter().writeValueAsString(entity) );
            }
        } catch( Exception e ) {
            failAndLog(entity.getClass().getSimpleName() + " instance serialization failed", e); 
        } 
        
        return out;
    }
    
    
}
