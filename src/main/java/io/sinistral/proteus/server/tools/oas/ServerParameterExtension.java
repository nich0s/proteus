/**
 * 
 */
package io.sinistral.proteus.server.tools.oas;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtensions;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * @author jbauer
 *
 */

public class ServerParameterExtension extends AbstractOpenAPIExtension {
    private static String QUERY_PARAM = "query";
    private static String HEADER_PARAM = "header";
    private static String COOKIE_PARAM = "cookie";
    private static String PATH_PARAM = "path";
    private static String FORM_PARAM = "form";

    final ObjectMapper mapper = Json.mapper();

    @Override
    public ResolvedParameter extractParameters(List<Annotation> annotations,
                                               Type type,
                                               Set<Type> typesToSkip,
                                               Components components,
                                               javax.ws.rs.Consumes classConsumes,
                                               javax.ws.rs.Consumes methodConsumes,
                                               boolean includeRequestBody,
                                               JsonView jsonViewAnnotation,
                                               Iterator<OpenAPIExtension> chain) {
    	
        if (shouldIgnoreType(type, typesToSkip)) {
            return new ResolvedParameter();
        }

    	JavaType javaType = constructType(type);
    	
    	boolean isRequired = true;

        if(_isOptionalType(javaType))
        {
        	isRequired = false;
        }
        
        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = null;
        ResolvedParameter extractParametersResult = new ResolvedParameter();
   
        
        for (Annotation annotation : annotations) {
            if (annotation instanceof QueryParam) {
                QueryParam param = (QueryParam) annotation;
                Parameter qp = new Parameter();
                qp.setIn(QUERY_PARAM);
                qp.setName(param.value());
                parameter = qp;
            } else if (annotation instanceof PathParam) {
                PathParam param = (PathParam) annotation;
                Parameter pp = new Parameter();
                pp.setIn(PATH_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof MatrixParam) {
                MatrixParam param = (MatrixParam) annotation;
                Parameter pp = new Parameter();
                pp.setIn(PATH_PARAM);
                pp.setStyle(Parameter.StyleEnum.MATRIX);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof HeaderParam) {
                HeaderParam param = (HeaderParam) annotation;
                Parameter pp = new Parameter();
                pp.setIn(HEADER_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof CookieParam) {
                CookieParam param = (CookieParam) annotation;
                Parameter pp = new Parameter();
                pp.setIn(COOKIE_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof io.swagger.v3.oas.annotations.Parameter) {
                if (((io.swagger.v3.oas.annotations.Parameter) annotation).hidden()) {
                    extractParametersResult.parameters = parameters;
                    return extractParametersResult;
                }
                if (parameter == null) {
                    Parameter pp = new Parameter();
                    parameter = pp;
                }
            } else {
                if (handleAdditionalAnnotation(parameters, annotation, type, typesToSkip, classConsumes, methodConsumes, components, includeRequestBody, jsonViewAnnotation)) {
                     
                	extractParametersResult.parameters.addAll(parameters);
                 }
            }
        }
        
     

        if (parameter != null && StringUtils.isNotBlank(parameter.getIn())) {
        	parameter.setRequired(isRequired);
             parameters.add(parameter);
        } else if (includeRequestBody) {
            Parameter unknownParameter = ParameterProcessor.applyAnnotations(
                    null,
                    type,
                    annotations,
                    components,
                    classConsumes == null ? new String[0] : classConsumes.value(),
                    methodConsumes == null ? new String[0] : methodConsumes.value(), jsonViewAnnotation);
            if (unknownParameter != null) {
 
                if (StringUtils.isNotBlank(unknownParameter.getIn()) && !"form".equals(unknownParameter.getIn())) {
                    extractParametersResult.parameters.add(unknownParameter);
                } else if ("form".equals(unknownParameter.getIn())) {
                    unknownParameter.setIn(null);
                    extractParametersResult.formParameter = unknownParameter; 
                } else {            // return as request body
                    extractParametersResult.requestBody = unknownParameter; 

                }
            }
        }
        for (Parameter p : parameters) {
        	
         	
            Parameter processedParameter = ParameterProcessor.applyAnnotations(
                    p,
                    type,
                    annotations,
                    components,
                    classConsumes == null ? new String[0] : classConsumes.value(),
                    methodConsumes == null ? new String[0] : methodConsumes.value(),
                    jsonViewAnnotation);
            
            if (processedParameter != null) {
            	processedParameter.setRequired(isRequired);
                extractParametersResult.parameters.add(processedParameter);
            }
        }
        return extractParametersResult;
    }
    
    protected boolean _isOptionalType(JavaType propType) {
        return Arrays.asList("com.google.common.base.Optional", "java.util.Optional")
                .contains(propType.getRawClass().getCanonicalName());
    }


    /**
     * Adds additional annotation processing support
     *
     * @param parameters
     * @param annotation
     * @param type
     * @param typesToSkip
     */

    private boolean handleAdditionalAnnotation(List<Parameter> parameters, Annotation annotation,
                                               final Type type, Set<Type> typesToSkip, javax.ws.rs.Consumes classConsumes,
                                               javax.ws.rs.Consumes methodConsumes, Components components, boolean includeRequestBody, JsonView jsonViewAnnotation) {
        boolean processed = false;
        if (BeanParam.class.isAssignableFrom(annotation.getClass())) {
        	
         	
            // Use Jackson's logic for processing Beans
        	JavaType javaType = constructType(type);
        	
            final BeanDescription beanDesc = mapper.getSerializationConfig().introspect(javaType);
            final List<BeanPropertyDefinition> properties = beanDesc.findProperties();

//        	if(extracted.size() == 0)
//        	{
//        		System.out.println("Unable to find parameters...");
//        		
//        		 Parameter processedParam = ParameterProcessor.applyAnnotations(
//        		                                                                p,
//        		                                                                paramType,
//        		                                                                paramAnnotations,
//        		                                                                components,
//        		                                                                classConsumes == null ? new String[0] : classConsumes.value(),
//        		                                                                methodConsumes == null ? new String[0] : methodConsumes.value(),
//        		                                                                jsonViewAnnotation);
//        	}
            
            for (final BeanPropertyDefinition propDef : properties) {
                final AnnotatedField field = propDef.getField();
                final AnnotatedMethod setter = propDef.getSetter();
                final AnnotatedMethod getter = propDef.getGetter();
                final List<Annotation> paramAnnotations = new ArrayList<Annotation>();
                final Iterator<OpenAPIExtension> extensions = OpenAPIExtensions.chain();
                Type paramType = null;

                // Gather the field's details
                if (field != null) {
                    paramType = field.getType();

                    AnnotationMap annotationMap = field.getAllAnnotations();
                    
                    for (final Annotation fieldAnnotation : annotationMap.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                // Gather the setter's details but only the ones we need
                if (setter != null) {
                    // Do not set the param class/type from the setter if the values are already identified
                    if (paramType == null) {
                        // paramType will stay null if there is no parameter
                        paramType = setter.getParameterType(0);
                    }

                    AnnotationMap annotationMap = setter.getAllAnnotations();

                    for (final Annotation fieldAnnotation : annotationMap.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                // Gather the getter's details but only the ones we need
                if (getter != null) {
                    // Do not set the param class/type from the getter if the values are already identified
                    if (paramType == null) {
                        paramType = getter.getType();
                    }

                    AnnotationMap annotationMap = getter.getAllAnnotations();

                    for (final Annotation fieldAnnotation : annotationMap.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                if (paramType == null) {
                    continue;
                }
                
 
                // Re-process all Bean fields and let the default swagger-jaxrs/swagger-jersey-jaxrs processors do their thing
                List<Parameter> extracted =
                        extensions.next().extractParameters(
                                paramAnnotations,
                                paramType,
                                typesToSkip,
                                components,
                                classConsumes,
                                methodConsumes,
                                includeRequestBody,
                                jsonViewAnnotation,
                                extensions).parameters;

             	


                for (Parameter p : extracted) {
                    Parameter processedParam = ParameterProcessor.applyAnnotations(
                            p,
                            paramType,
                            paramAnnotations,
                            components,
                            classConsumes == null ? new String[0] : classConsumes.value(),
                            methodConsumes == null ? new String[0] : methodConsumes.value(),
                            jsonViewAnnotation);
                    if (processedParam != null) {
                    	
                    	System.out.println("added new parameters: " + processedParam);
                        parameters.add(processedParam);
                    }
                }

                processed = true;
            }
        }
        return processed;
    }

    @Override
    protected boolean shouldIgnoreClass(Class<?> cls) {
        return cls.getName().startsWith("javax.ws.rs.") || cls.getName().startsWith("io.undertow");
    
    }
}
