package info.jultest.test.module;

import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.loading.ILoadingState;

import java.util.List;

// A simple implementation of ILoadingState that's only for testing purpose.
public class TestLoadingState implements ILoadingState {
	
	private String typeName;
	private List<String> dependedTypeNames;
	private boolean isAttribute;
	
	public TestLoadingState(String typeName, List<String> dependedTypeNames, boolean isAttribute){
		this.typeName = typeName;
		this.dependedTypeNames = dependedTypeNames;
		this.isAttribute = isAttribute;
	}
	
	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public List<String> getDependentTypeNames() {
		return dependedTypeNames;
	}

	@Override
	public boolean isAttributeType() {
		return isAttribute;
	}

	@Override
	public boolean isSealed() { return true; }

	@Override
	public boolean isParsed() { return true; }

	@Override
	public void setParsed()  { }

	@Override
	public boolean isFaulted()  { return false; }

	@Override
	public void setFaulted(Exception ex)  { }

	@Override
	public void addDependency(String dep) { }

	@Override
	public Thread getOwner() { return null; }

	@Override
	public JClassTypeBuilder getBuilder() { return null; }

	@Override
	public Exception getException() { return null; }
	
	@Override
	public JDefinedClassType getType() { return null; }	
}
