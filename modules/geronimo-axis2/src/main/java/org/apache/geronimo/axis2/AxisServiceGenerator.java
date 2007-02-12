/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis2;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ResponseWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebResultAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

public class AxisServiceGenerator {
	
	public AxisServiceGenerator(){
		super();
	}
	
	public AxisService getServiceFromWSDL(org.apache.geronimo.jaxws.PortInfo portInfo, String endpointClassName, Definition wsdlDefinition, ClassLoader classLoader) throws Exception {
		WSDLToAxisServiceBuilder wsdlBuilder = null;
   		
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLWriter writer = factory.newWSDLWriter();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeWSDL(wsdlDefinition, out);
        String wsdlContent = out.toString("UTF-8"); //TODO WSDL Doc must be either UTF-8 or UTF-16
        
   		OMNamespace documentElementNS = ((OMElement)XMLUtils.toOM(new StringReader(wsdlContent))).getNamespace();
   		
   		Map<QName, Service> serviceMap = wsdlDefinition.getServices();
    	Service wsdlService = serviceMap.values().iterator().next();
    	
    	Map<String, Port> portMap = wsdlService.getPorts();
    	Port port = portMap.values().iterator().next();
    	String portName = port.getName();
   		QName serviceQName = wsdlService.getQName();

   		//Decide WSDL Version : 
    	if(WSDLConstants.WSDL20_2006Constants.DEFAULT_NAMESPACE_URI.equals(documentElementNS.getNamespaceURI())){
    		wsdlBuilder = new WSDL20ToAxisServiceBuilder(new StringBufferInputStream(wsdlContent), serviceQName, null);
    	}
    	else if(Constants.NS_URI_WSDL11.equals(documentElementNS.getNamespaceURI())){
    		wsdlBuilder = new WSDL11ToAxisServiceBuilder(wsdlDefinition, serviceQName , portName);
    	}
    	//populate with axis2 objects
    	AxisService service = wsdlBuilder.populateService();
    	service.addParameter(new Parameter("ServiceClass", endpointClassName));
        service.setWsdlFound(true);
        service.setClassLoader(classLoader);
    	
    	//Going to create annotations by hand
    	DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
    	dbc.setClassLoader(classLoader);
    	HashMap<String, DescriptionBuilderComposite> dbcMap = new HashMap<String, DescriptionBuilderComposite>();
    	
    	//Service related annotations
        WebServiceAnnot serviceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        serviceAnnot.setPortName(portName);
        serviceAnnot.setServiceName(service.getName());
        serviceAnnot.setName(service.getName());
        serviceAnnot.setTargetNamespace(service.getTargetNamespace());
  	
   	 	Class endPointClass = classLoader.loadClass(endpointClassName);
 		Method[] classMethods = endPointClass.getMethods();
 		
   	 	for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
   	 		AxisOperation operation = opIterator.next();
   	 		operation.setMessageReceiver(JAXWSMessageReceiver.class.newInstance());
   	 		
   	 		for(Method method : classMethods){
   	 			String axisOpName = operation.getName().getLocalPart();
   	 			if(method.getName().equals(axisOpName)){
   	 				fillOperationInformation(method, operation, dbc);
   	 				break;
   	 			}
   	 		}
   	 	}
    	
    	dbc.setWebServiceAnnot(serviceAnnot);
   	 	dbc.setWsdlDefinition(wsdlDefinition);
   	 	dbc.setClassName(endpointClassName);
   	 	dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));
   	 	dbcMap.put(endpointClassName, dbc);
        List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        ServiceDescription sd = serviceDescList.get(0);
        Parameter serviceDescription = new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, sd.getEndpointDescriptions()[0]);
        service.addParameter(serviceDescription);
        
        return service;
	}
	
	private void fillOperationInformation(Method method, AxisOperation operation, DescriptionBuilderComposite dbc) throws Exception{
			
			MethodDescriptionComposite mdc = new MethodDescriptionComposite();
			WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
			webMethodAnnot.setOperationName(method.getName());
			
			//TODO: Might need implement more here
//			if(operation.getStyle().equals(AxisOperation.STYLE_DOC)){
//				
//			}else if(operation.getStyle().equals(AxisOperation.STYLE_RPC)){
//				
//			}
			
			
			mdc.setWebMethodAnnot(webMethodAnnot);
			mdc.setMethodName(method.getName());

	 		String MEP = operation.getMessageExchangePattern();
	 		
	 		if (WSDLUtil.isInputPresentForMEP(MEP)) {
	 			AxisMessage inAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
	 			if(inAxisMessage != null){
	 				
	 				XmlSchemaType schemaType = inAxisMessage.getSchemaElement().getSchemaType();
	 				
	 				if(schemaType instanceof XmlSchemaComplexType){
	 					XmlSchemaComplexType complexSchemaType = (XmlSchemaComplexType)schemaType;
	 					
	 					if(complexSchemaType.getParticle() instanceof XmlSchemaSequence){
	 						XmlSchemaSequence sequence = (XmlSchemaSequence)complexSchemaType.getParticle();
	 						for(Iterator iterator = sequence.getItems().getIterator(); iterator.hasNext();){
	 							XmlSchemaObject xmlObject = (XmlSchemaObject)iterator.next();
	 							
	 							if(xmlObject instanceof XmlSchemaElement){
	 								XmlSchemaElement xmlElement = (XmlSchemaElement)xmlObject;
	 								
	 								ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
	 								WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
           	 				
	 								webParamAnnot.setName(xmlElement.getName());
	 								pdc.setWebParamAnnot(webParamAnnot);
	 								
	 								Class[] paramTypes = method.getParameterTypes();
	 								
	 								for(Class paramType : paramTypes){
	 									String strParamType = paramType.toString();
	 									pdc.setParameterType(strParamType.split(" ")[1]);
	 								}
	 								
	 								mdc.addParameterDescriptionComposite(pdc);
	 							}
	 							
	 						}
	 					}
	 				}
	 			}
	 		}
	 		
	 		if (WSDLUtil.isOutputPresentForMEP(MEP)) {
	 			AxisMessage outAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
	 			
	 			if(outAxisMessage != null){

	 				XmlSchemaType schemaType = outAxisMessage.getSchemaElement().getSchemaType();
	 				mdc.setReturnType(method.getReturnType().toString().split(" ")[1]);
	 				
	   	 			if(schemaType instanceof XmlSchemaComplexType){
		 					XmlSchemaComplexType complexSchemaType = (XmlSchemaComplexType)schemaType;
		 					
		 					if(complexSchemaType.getParticle() instanceof XmlSchemaSequence){
		 						XmlSchemaSequence sequence = (XmlSchemaSequence)complexSchemaType.getParticle();
		 						for(Iterator iterator = sequence.getItems().getIterator(); iterator.hasNext();){
		 							XmlSchemaObject xmlObject = (XmlSchemaObject)iterator.next();
		 							
		 							if(xmlObject instanceof XmlSchemaElement){
		 								XmlSchemaElement xmlElement = (XmlSchemaElement)xmlObject;
		 								WebResultAnnot webResult = WebResultAnnot.createWebResultAnnotImpl();
		 								webResult.setName(xmlElement.getName());
		 								mdc.setWebResultAnnot(webResult);
		 							}
		 						}
		 					}
		 				}
		 				
		 				ResponseWrapperAnnot responseWrap = ResponseWrapperAnnot.createResponseWrapperAnnotImpl();
		 				responseWrap.setClassName(getWrapperClassName(outAxisMessage.getElementQName()));
		 				mdc.setResponseWrapperAnnot(responseWrap);
		 			}
	 		}
	 		mdc.setWebMethodAnnot(webMethodAnnot);
	 		dbc.addMethodDescriptionComposite(mdc);
	}
	
	//TODO: Has to verify how JAXB default class wrapper class generation logic
	private String getWrapperClassName(QName element) throws Exception {
		String localPart = element.getLocalPart();
		String nameSpace = element.getNamespaceURI();
		
		URI nameSpaceURI = new URI(nameSpace);
		
		String host = nameSpaceURI.getHost();
		String path = nameSpaceURI.getPath();
		String[] pathParts = path.split("/");
		int index = 0;
		
		while(index < host.length()){
			if(host.charAt(index) == '.'){
				break;
			}
			index ++;
		}
		
		String packageName = host.substring(index+1, host.length())+"."+host.substring(0, index); 
		
		for(String pathPart : pathParts){
			if(!pathPart.equals("")){
				packageName += ("."+pathPart);
			}
		}
		
		String className = localPart;
		if(Character.isLowerCase(localPart.charAt(0))){
			className = (char)Character.toUpperCase(localPart.charAt(0))+className.substring(1, className.length());
		}
				
		return packageName + "." + className;
	}
	
    private class WSDLGeneratorImpl implements WsdlGenerator {

    	private Definition def;
    	
    	public WSDLGeneratorImpl(Definition def) {
    		this.def = def;
    	}
    	
    	public WsdlComposite generateWsdl(String implClass, String bindingType) throws WebServiceException {
    		// Need WSDL generation code
    		WsdlComposite composite = new WsdlComposite();
    		composite.setWsdlFileName(implClass);
    		HashMap<String, Definition> testMap = new HashMap<String, Definition>();
    		testMap.put(composite.getWsdlFileName(), def);
    		composite.setWsdlDefinition(testMap);
    		return composite;
    	}
    }
}
