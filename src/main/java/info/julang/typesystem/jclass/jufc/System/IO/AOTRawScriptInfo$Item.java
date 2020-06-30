package info.julang.typesystem.jclass.jufc.System.IO;

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
public class AOTRawScriptInfo$Item extends RawScriptInfo {
    
    private List<RequirementInfo> m_requirements;
    private List<RawClassInfo> m_classes;
    
    public AOTRawScriptInfo$Item () {
        FoundationClassParser parser = new FoundationClassParser("System/IO/Item.jul");
        ainfo = new LazyAstInfo(parser, "System/IO/Item.jul", null);
        
        m_classes = new ArrayList<RawClassInfo>();
        m_classes.add(new RawClassInfo("Item", new AOTClassDeclInfo_Item (this)));

        m_requirements = new ArrayList<RequirementInfo>();
        m_requirements.add(new RequirementInfo("System", null));
    }

    public String getModuleName() {
        return "System.IO";
    }

    public List<RequirementInfo> getRequirements() {
        return m_requirements;
    }

    public List<RawClassInfo> getClasses() {
        return m_classes;
    }

    public String getScriptFilePath() {
        return "System/IO/Item.jul";
    }
    
    // Types declared in this script //
    
        
    class AOTClassDeclInfo_Item extends LazyClassDeclInfo {

        public AOTClassDeclInfo_Item(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("ItemExtension"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.IO.Item");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "Item";
        }

        public ClassSubtype getSubtype() {
            return ClassSubtype.INTERFACE;
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