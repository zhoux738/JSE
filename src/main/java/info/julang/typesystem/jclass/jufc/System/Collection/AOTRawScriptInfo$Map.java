package info.julang.typesystem.jclass.jufc.System.Collection;

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
public class AOTRawScriptInfo$Map extends RawScriptInfo {
    
    private List<RequirementInfo> m_requirements;
    private List<RawClassInfo> m_classes;
    
    public AOTRawScriptInfo$Map () {
        FoundationClassParser parser = new FoundationClassParser("System/Collection/Map.jul");
        ainfo = new LazyAstInfo(parser, "System/Collection/Map.jul", null);
        
        m_classes = new ArrayList<RawClassInfo>();
        m_classes.add(new RawClassInfo("Map", new AOTClassDeclInfo_Map (this)));
        m_classes.add(new RawClassInfo("MapIterator", new AOTClassDeclInfo_MapIterator (this)));
        m_classes.add(new RawClassInfo("HashKey", new AOTClassDeclInfo_HashKey (this)));

        m_requirements = new ArrayList<RequirementInfo>();
        m_requirements.add(new RequirementInfo("System", null));
        m_requirements.add(new RequirementInfo("System.Util", null));
    }

    public String getModuleName() {
        return "System.Collection";
    }

    public List<RequirementInfo> getRequirements() {
        return m_requirements;
    }

    public List<RawClassInfo> getClasses() {
        return m_classes;
    }

    public String getScriptFilePath() {
        return "System/Collection/Map.jul";
    }
    
    // Types declared in this script //
    
        
    class AOTClassDeclInfo_Map extends LazyClassDeclInfo {

        public AOTClassDeclInfo_Map(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("Container"));
            m_parentNames.add(ParsedTypeName.makeFromFullName("IIndexable"));
            m_parentNames.add(ParsedTypeName.makeFromFullName("IIterable"));
            m_parentNames.add(ParsedTypeName.makeFromFullName("IMapInitializable"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Collection.Map");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "Map";
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
    
        
    class AOTClassDeclInfo_MapIterator extends LazyClassDeclInfo {

        public AOTClassDeclInfo_MapIterator(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("IIterator"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Collection.MapIterator");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "MapIterator";
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
            return Accessibility.MODULE;
        }
        
        public boolean isAccessibilitySet(){
            return true;
        }

    }
    
        
    class AOTClassDeclInfo_HashKey extends LazyClassDeclInfo {

        public AOTClassDeclInfo_HashKey(RawScriptInfo minfo) {
            super(minfo);
            
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Collection.HashKey");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "HashKey";
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
            return Accessibility.MODULE;
        }
        
        public boolean isAccessibilitySet(){
            return true;
        }

    }
}