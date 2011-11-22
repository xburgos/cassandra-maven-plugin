package org.apache.maven.diagrams.connector_api.descriptor;

/**
 * This class represents hint to the xstream library during the
 * serialization/deserialization project.
 * 
 * It binds clazz with tagName. It also (optionally) can bind it to the
 * converter class.
 * 
 * @author Piotr Tabor
 */
public class Mapping {

	private Class<?> class_;

	private String tagName;

	/** How to convert the tag content into the clazz instance */
	private Class<?> converter;

	public Mapping(Class<?> clazz, String tagName) {
		super();
		this.class_ = clazz;
		this.tagName = tagName;
	}

	public Mapping() {
		super();
	}

	public Class<?> getClazz() {
		return class_;
	}

	public void setClass_(Class<?> clazz) {
		this.class_ = clazz;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Class<?> getConverter() {
		return converter;
	}

	public void setConverter(Class<?> converter) {
		this.converter = converter;
	}

	public void setClassName(String className) throws ClassNotFoundException {
		class_ = Class.forName(className);
	}

	public void setConverterClassName(String className)
			throws ClassNotFoundException {
		converter = Class.forName(className);
	}

}
