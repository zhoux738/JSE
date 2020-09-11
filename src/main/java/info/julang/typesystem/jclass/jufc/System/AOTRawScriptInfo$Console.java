package info.julang.typesystem.jclass.jufc.System;

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
public class AOTRawScriptInfo$Console extends RawScriptInfo {
    
    private List<RequirementInfo> m_requirements;
    private List<RawClassInfo> m_classes;
    
    public AOTRawScriptInfo$Console () {
        FoundationClassParser parser = new FoundationClassParser("System/Console.jul");
        ainfo = new LazyAstInfo(parser, "System/Console.jul", null);
        
        m_classes = new ArrayList<RawClassInfo>();
        m_classes.add(new RawClassInfo("Console", new AOTClassDeclInfo_Console (this)));

        m_requirements = new ArrayList<RequirementInfo>();
        m_requirements.add(new RequirementInfo("System", null));
    }

    public String getModuleName() {
        return "System";
    }

    public List<RequirementInfo> getRequirements() {
        return m_requirements;
    }

    public List<RawClassInfo> getClasses() {
        return m_classes;
    }

    public String getScriptFilePath() {
        return "System/Console.jul";
    }
    
    // Types declared in this script //
    
        
    class AOTClassDeclInfo_Console extends LazyClassDeclInfo {

        public AOTClassDeclInfo_Console(RawScriptInfo minfo) {
            super(minfo);
            
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Console");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "Console";
        }

        public ClassSubtype getSubtype() {
            return ClassSubtype.CLASS;
        }
        
        public boolean isFinal() {
            return true;
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
            return true;
        }

        public Accessibility getAccessibility() {
            return Accessibility.PUBLIC;
        }
        
        public boolean isAccessibilitySet(){
            return true;
        }

    }
}