package info.julang.typesystem.jclass.jufc.System.Reflection;

import java.util.ArrayList;
import java.util.List;

import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.RequirementInfo;
import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.prescanning.LazyClassDeclInfo;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.jufc.FoundationClassParser;


/**
 * This source file is automatically generated.
 */
public class AOTRawScriptInfo$Method extends RawScriptInfo {
    
    private List<RequirementInfo> m_requirements;
    private List<RawClassInfo> m_classes;
    
    public AOTRawScriptInfo$Method () {
        FoundationClassParser parser = new FoundationClassParser("System/Reflection/Method.jul");
        ainfo = new LazyAstInfo(parser, "System/Reflection/Method.jul", null);
        
        m_classes = new ArrayList<RawClassInfo>();
        m_classes.add(new RawClassInfo("Method", new AOTClassDeclInfo_Method (this)));

        m_requirements = new ArrayList<RequirementInfo>();
        m_requirements.add(new RequirementInfo("System", null));
        m_requirements.add(new RequirementInfo("System.Concurrency", null));
    }

    public String getModuleName() {
        return "System.Reflection";
    }

    public List<RequirementInfo> getRequirements() {
        return m_requirements;
    }

    public List<RawClassInfo> getClasses() {
        return m_classes;
    }

    public String getScriptFilePath() {
        return "System/Reflection/Method.jul";
    }
    
    // Types declared in this script //
    
        
    class AOTClassDeclInfo_Method extends LazyClassDeclInfo {

        public AOTClassDeclInfo_Method(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("Member"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Reflection.Method");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "Method";
        }

        public ClassSubtype getSubtype() {
            return ClassSubtype.CLASS;
        }
        
        public boolean isFinal() {
            return false;
        }
        
        public boolean isConst() {
            return false;
        }
        
        public boolean isHosted() {
            return false;
        }
        
        public boolean isAbstract() {
            return false;
        }

        public boolean isStatic() {
            return false;
        }

        public Accessibility getAccessibility() {
            return Accessibility.PUBLIC;
        }
        
        public boolean isAccessibilitySet(){
            return true;
        }

    }
}