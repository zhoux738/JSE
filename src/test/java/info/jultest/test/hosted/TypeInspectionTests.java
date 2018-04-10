package info.jultest.test.hosted;

import info.julang.hosting.mapped.IllegalTypeMappingException;
import info.julang.hosting.mapped.inspect.DeferredMappedType;
import info.julang.hosting.mapped.inspect.IMappedType;
import info.julang.hosting.mapped.inspect.KnownMappedType;
import info.julang.hosting.mapped.inspect.MappedConstructorInfo;
import info.julang.hosting.mapped.inspect.MappedFieldInfo;
import info.julang.hosting.mapped.inspect.MappedMethodInfo;
import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.hosting.mapped.inspect.PlatformTypeMapper;
import info.julang.typesystem.jclass.builtin.JStringType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TypeInspectionTests {

	@Test
	public void mapFoundationClassTest() throws IllegalTypeMappingException {
		PlatformTypeMapper mapper = new PlatformTypeMapper();
		MappedTypeInfo mti = mapper.mapType(TypeInspectionTests.class.getClassLoader(), "java.io.File", null, true);

		boolean checked = false;
		
		// Check one of them:
		//   File(File parent, String child)
		//   Creates a new File instance from a parent abstract pathname and a child pathname string.
		List<MappedConstructorInfo> ctors = mti.getConstructors();
		for(MappedConstructorInfo mci : ctors) {
			IMappedType[] ptyps = mci.getParamTypes();
			if (ptyps.length == 2) {
				boolean p0 = false, p1 = false;
				if (ptyps[0].isExternal()) {
					DeferredMappedType dmt = (DeferredMappedType)ptyps[0];
					p0 = dmt.isSameToEnclosingType() && dmt.getFullClassName().equals("java.io.File");
 				}
				if (!ptyps[1].isExternal()) {
					KnownMappedType kmt = (KnownMappedType)ptyps[1];
					p1 = kmt.getType() == JStringType.getInstance();
 				}
				
				checked = p0 && p1;
				if (checked) {
					break;
				}
			}
		}
		
		Assert.assertTrue("Constructor File(File parent, String child) is not present.", checked);
		
		// Check one of them:
		//   static String 	pathSeparator
		//   The system-dependent path-separator character, represented as a string for convenience.
		List<MappedFieldInfo> fields = mti.getFields();
		checked = false;
		for(MappedFieldInfo mfi : fields) {
			IMappedType mtyp = mfi.getType();
			if (mfi.isStatic() && mfi.isFinal() && mfi.getName().equals("pathSeparator") && !mtyp.isExternal()) {
				KnownMappedType kmt = (KnownMappedType)mtyp;
				if (kmt.getType() == JStringType.getInstance()){
					checked = true;
					break;
				}
			}
		}
		
		Assert.assertTrue("Field pathSeparator (static, final) is not present.", checked);

		// Check one of them:
		//   File[] 	listFiles(FileFilter filter)
		//   Returns an array of abstract pathnames denoting the files and directories in the directory 
		//   denoted by this abstract pathname that satisfy the specified filter.
		//   (since Java 1.2)
		List<MappedMethodInfo> methods = mti.getMethods();
		checked = false;
		for(MappedMethodInfo mmi : methods) {
			if (!mmi.getName().equals("listFiles")) {
				continue;
			}
			
			// Return type
			IMappedType mtyp = mmi.getType();
			if (!mmi.isStatic() && 
			    mtyp.isExternal() && 
				mtyp.getDimension() == 1 && 
				mtyp.getOriginalClass() == File[].class) {
				// Parameter type
				IMappedType[] ptyps = mmi.getParamTypes();
				if (ptyps.length == 1) {
					if (ptyps[0].isExternal()) {
						DeferredMappedType dmt = (DeferredMappedType)ptyps[0];
						if (dmt.getFullClassName().equals("java.io.FileFilter")){
							checked = true;
							break;
						}
	 				}
				}
			}
		}
		
		Assert.assertTrue("Method listFiles(FileFilter filter):File[] is not present.", checked);
	}

	@SuppressWarnings("unused")
	public static class TestClass {
		public static final int pubStaFin = 1;
		private static final int privStaFin = 1;
		public static int pubSta = 1;
		public final int pubFin = 1;
		
		private void privateMethod(){}
		private static void privateStaticMethod(){}
	}
	
	@Test
	public void mapCustomizedClassTest() throws IllegalTypeMappingException {
		PlatformTypeMapper mapper = new PlatformTypeMapper();
		MappedTypeInfo mti = mapper.mapType(
			TypeInspectionTests.class.getClassLoader(), 
			"info.jultest.test.hosted.TypeInspectionTests$TestClass", null, true);
		
		// Check fields
		List<MappedFieldInfo> fields = mti.getFields();
		Set<String> unexpFields = new HashSet<String>();
		unexpFields.add("privStaFin");
		String unexpName = null;
		for(MappedFieldInfo mfi : fields) {
			if (unexpFields.contains(mfi.getName())){
				unexpName = mfi.getName();
				break;
			}
		}

		Assert.assertNull("Member " + unexpName + " shouldn't be present in the mapped type info.", unexpName);
		
		// Check methods
		List<MappedMethodInfo> methods = mti.getMethods();
		unexpFields = new HashSet<String>();
		unexpFields.add("privateMethod");
		unexpFields.add("privateStaticMethod");
		unexpName = null;
		for(MappedMethodInfo mmi : methods) {
			if (unexpFields.contains(mmi.getName())){
				unexpName = mmi.getName();
				break;
			}
		}

		Assert.assertNull("Member " + unexpName + " shouldn't be present in the mapped type info.", unexpName);
		
	}
}
