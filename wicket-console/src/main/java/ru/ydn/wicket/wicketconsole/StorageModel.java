package ru.ydn.wicket.wicketconsole;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

public class StorageModel<T> implements IModel<T>{
	
	private Serializable serializableValue;
	private transient Object nonSerializableValue;
	private boolean valueWasDefined;
	
	public StorageModel() {
	}
	
	public StorageModel(T value) {
		setObject(value);
	}

	@Override
	public T getObject() {
		if(serializableValue!=null) return (T)serializableValue;
		else if(nonSerializableValue!=null) return (T)nonSerializableValue;
		else {
			if(valueWasDefined) {
				T resurected = resurect();
				if(resurected!=null) setObject(resurected);
				return resurected;
			} else {
				return null;
			}
		}
	}
	
	@Override
	public void setObject(T object) {
		serializableValue = null;
		nonSerializableValue = null;
		if(object instanceof Serializable) {
			serializableValue = (Serializable) object;
		} else {
			nonSerializableValue = object;
		}
		valueWasDefined = object!=null;
	}
	
	private T resurect() {
		return null;
	}
	
	@Override
	public void detach() {
		
	}
}
