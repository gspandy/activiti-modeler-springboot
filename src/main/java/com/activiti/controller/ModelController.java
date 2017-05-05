package com.activiti.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.StreamResource.StreamSource;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/model")
public class ModelController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ModelController.class);

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * 模型列表
	 */
	@RequestMapping("/list")
	public Object list() {
		List<Model> list = repositoryService.createModelQuery().list();
		return list;
	}

	/** 获取图片 */
	@RequestMapping("/getImage")
	public void getImage(String modelId, HttpServletResponse response) {
		StreamSource streamSource = null;
		final byte[] editorSourceExtra = repositoryService.getModelEditorSourceExtra(modelId);
		if (editorSourceExtra != null) {
			streamSource = new StreamSource() {
				private static final long serialVersionUID = 1L;

				public InputStream getStream() {
					InputStream inStream = null;
					try {
						inStream = new ByteArrayInputStream(editorSourceExtra);
					} catch (Exception e) {
						LOGGER.warn("Error reading PNG in StreamSource", e);
					}
					return inStream;
				}
			};
		}
		if (streamSource != null) {
			response.setContentType("application/x-msdownload;charset=UTF-8");
			response.reset();// 清除缓冲中的数据
			int temp;
			OutputStream os = null;
			InputStream is = streamSource.getStream();
			try {
				os = response.getOutputStream();
				while ((temp = is.read()) != (-1)) {
					os.write(temp);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * 创建模型
	 */
	@RequestMapping(value = "/create", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public void create(@RequestParam("name") String name, @RequestParam("key") String key,
			@RequestParam(value = "description", required = false) String description, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION,
					org.apache.commons.lang3.StringUtils.defaultString(description));
			Model newModel = repositoryService.newModel();
			newModel.setMetaInfo(modelObjectNode.toString());
			newModel.setName(name);
			newModel.setKey(key);
			repositoryService.saveModel(newModel);
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.put("stencilset", stencilSetNode);
			repositoryService.addModelEditorSource(newModel.getId(), editorNode.toString().getBytes("utf-8"));
			System.out.println(newModel.getId() + "===========newModel.getId()==============");
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + newModel.getId());
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * 编辑模型前,读取模型
	 */
	@RequestMapping(value = "/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
	public ObjectNode getEditorJson(@PathVariable String modelId) {
		ObjectNode modelNode = null;

		Model model = repositoryService.getModel(modelId);

		if (model != null) {
			try {
				if (StringUtils.isNotEmpty(model.getMetaInfo())) {
					modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
				} else {
					modelNode = objectMapper.createObjectNode();
					modelNode.put(MODEL_NAME, model.getName());
				}
				modelNode.put(MODEL_ID, model.getId());
				ObjectNode editorJsonNode = (ObjectNode) objectMapper
						.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
				modelNode.put("model", editorJsonNode);

			} catch (Exception e) {
				LOGGER.error("Error creating model JSON", e);
				throw new ActivitiException("Error creating model JSON", e);
			}
		}
		return modelNode;
	}

	/**
	 * 保存模型
	 */
	@RequestMapping(value = "/{modelId}/save", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
		try {

			Model model = repositoryService.getModel(modelId);

			ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

			modelJson.put(MODEL_NAME, values.getFirst("name"));
			modelJson.put(MODEL_DESCRIPTION, values.getFirst("description"));
			model.setMetaInfo(modelJson.toString());
			model.setName(values.getFirst("name"));

			repositoryService.saveModel(model);

			repositoryService.addModelEditorSource(model.getId(), values.getFirst("json_xml").getBytes("utf-8"));

			InputStream svgStream = new ByteArrayInputStream(values.getFirst("svg_xml").getBytes("utf-8"));
			TranscoderInput input = new TranscoderInput(svgStream);

			PNGTranscoder transcoder = new PNGTranscoder();
			// Setup output
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			TranscoderOutput output = new TranscoderOutput(outStream);

			// Do the transformation
			transcoder.transcode(input, output);
			final byte[] result = outStream.toByteArray();
			repositoryService.addModelEditorSourceExtra(model.getId(), result);
			outStream.close();

		} catch (Exception e) {
			LOGGER.error("Error saving model", e);
			throw new ActivitiException("Error saving model", e);
		}
	}

	/**
	 * 删除模型
	 */
	@RequestMapping(value = "/delete/{modelId}")
	public String delete(@PathVariable("modelId") String modelId) {
		repositoryService.deleteModel(modelId);
		return "OK";
	}

	/**
	 * 导出model的xml文件
	 */
	@RequestMapping("/export")
	public void export(String modelId, HttpServletResponse response) {
		try {
			Model modelData = repositoryService.getModel(modelId);
			BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
			JsonNode editorNode = new ObjectMapper()
					.readTree(repositoryService.getModelEditorSource(modelData.getId()));
			BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
			BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
			byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

			ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
			IOUtils.copy(in, response.getOutputStream());
			String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.flushBuffer();
		} catch (Exception e) {
			LOGGER.error("导出model的xml文件失败：modelId={}", modelId, e);
		}
	}
}
