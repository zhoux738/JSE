package info.jultest.test.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import info.julang.typesystem.loading.depresolving.CyclicDependencyException;
import info.julang.typesystem.loading.depresolving.HardDependencyResolver;
import info.julang.typesystem.loading.depresolving.IDependencyResolver;
import info.julang.typesystem.loading.depresolving.IOrderResolvable;

public class ClassLoadingResolverTests {

	@Test(expected=CyclicDependencyException.class)
	public void simpleCyclicDependencyDetectionTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"A", "B"},
				new String[]{"C", "A"},
				new String[]{"B", "C"}
			}
		, false);
		list = resolve(list);
		validate(list);
	}
	
	@Test
	public void resolveWithoutSimpleDependencyTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"A4"},
				new String[]{"A3", "A4"},
				new String[]{"A2", "A3"},
				new String[]{"A1", "A2"}
			}
		, false);
		list = resolve(list);
		validate(list);
	}
	
	@Test
	public void resolveWithoutDependencyTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"A"},
				new String[]{"C"},
				new String[]{"B"}
			}
		, false);
		list = resolve(list);
		validate(list);
	}
	
	@Test(expected=CyclicDependencyException.class)
	public void NoDependencyOnItselfTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"A", "A"}
			}
		, false);
		list = resolve(list);
		validate(list);
	}
	
	@Test
	public void dependencyResolveInclusiveInputsTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"K"},
				new String[]{"A", "B", "C", "D"},
				new String[]{"B", "C", "D", "F"},
				new String[]{"D", "K"},
				new String[]{"E", "C", "F", "A"},
				new String[]{"M", "A", "B"},
				new String[]{"C", "F"},
				new String[]{"F"},
				
			}
		, false);
		List<? extends IOrderResolvable> sortedList = resolve(list);
		validate(sortedList);
	}
	
	@Test
	public void dependencyResolveNonInclusiveInputsTest(){
		List<? extends IOrderResolvable> list = createDependencyScenario(
			new String[][]{
				new String[]{"K"},
				new String[]{"A", "B", "C", "D"},
				new String[]{"B", "C", "D", "F"},
				new String[]{"D", "K"},
				new String[]{"E", "C", "F", "A"},
				new String[]{"M", "A", "B"},
				new String[]{"C", "F"}
				
			}
		, false);
		List<? extends IOrderResolvable> sortedList = resolve(list);
		validate(sortedList);
		
		boolean isThereF = false;
		for(IOrderResolvable state:sortedList) {
			if(state.getTypeName().equals("F")) {
				isThereF = true;
			}
		}
		if(isThereF) {
			org.junit.Assert.fail("The sorted IResolvable collection should not contain ones that only apprear as"
					+ "dependencies");
		}
	}
	
	@Test
	public void resolveTestAttributesLoadedFirstTest(){
		List<? extends IOrderResolvable> classList = createDependencyScenario(
			new String[][]{
				new String[]{"A", "B"},
				new String[]{"C", "D"},
				new String[]{"D", "E"}
			}
		, false);
		List<? extends IOrderResolvable> attributeList = createDependencyScenario(
			new String[][]{
				new String[]{"K", "M"},
				new String[]{"M", "System.Attribute"},
				new String[]{"System.Attribute"}
			}
		, true);
		
		@SuppressWarnings("unchecked")
        List<TestLoadingState> allList = (List<TestLoadingState>) classList;
		for(IOrderResolvable attri:attributeList) {
			allList.add((TestLoadingState) attri);
		}
		
		int count = classList.size();
		List<IOrderResolvable> list = resolve(classList);
		validate(list);
		if(count != list.size()) {
			org.junit.Assert.fail("The size of sorted IResolvable collection should equal to the given collection");
		}
		int attributeCount = attributeList.size();
		for(IOrderResolvable state:attributeList) {
			if(list.indexOf(state) >= attributeCount) {
				org.junit.Assert.fail("Attribute " + state.getTypeName() + " should be loaded before class");
			}
		}
	}	
	
	private List<IOrderResolvable> resolve(List<? extends IOrderResolvable> list) {
		IDependencyResolver resolver = new HardDependencyResolver(); 
		return resolver.resolve(list);
	}

	private List<? extends IOrderResolvable> createDependencyScenario(String[][] inputs, boolean isAttribute){
		List<TestLoadingState> result = new ArrayList<TestLoadingState>();
		for(int i = 0; i < inputs.length; i++){
			String[] row = inputs[i];
			String tname = row[0];
			
			List<String> list = new ArrayList<String>();
			for(int j = 1; j < row.length; j++){
				list.add(row[j]);
			}
			
			result.add(new TestLoadingState(tname, list, isAttribute));
		}
		
		return result;
	}
	
	private void validate(List<? extends IOrderResolvable> ordered){
		// Get anti-dependency table
		//  A --dep--> [B]
		//  C --dep--> [B, D]
		// =>
		//  B --prec-> {A, C}
		//  D --prec-> {C}
		
		Map<String, Set<String>> antiDepMap = new HashMap<String, Set<String>>();
		// For each loading state,
		for(IOrderResolvable state : ordered){
			// For each dependency, 
			for(String key : state.getDependentTypeNames()){
				// Add anti-dependency
				Set<String> result = antiDepMap.get(key);
				if(result == null){
					result = new HashSet<String>();
					antiDepMap.put(key, result);
				}
				result.add(state.getTypeName());
			}
		}
		
		int size = ordered.size();
		for(int i = 1; i < size; i++){ // Start from the second since there is no need to check the first one.
			IOrderResolvable state = ordered.get(i);
			String name1 = state.getTypeName();
			Set<String> antiDeps = antiDepMap.get(name1);
			if(antiDeps == null || antiDeps.isEmpty()){
				continue;
			} else {
				for(int j = 0; j < i; j++){
					String name2 = ordered.get(j).getTypeName();
					boolean contained = antiDeps.contains(name2);
					if(contained){
						org.junit.Assert.fail(
							"Dependency check failed. Class \"" + 
							name1 + 
							"\" is loaded after \"" + 
							name2 + "\", but \"" + 
							name2 + "\" requires \"" + 
							name1 + "\" to be loaded first.");
					}
				}
			}
		}
	}
	
}
