package info.julang.typesystem.jclass.jufc.System.Lang;

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
public class AOTRawScriptInfo$Exception extends RawScriptInfo {
    
    private List<RequirementInfo> m_requirements;
    private List<RawClassInfo> m_classes;
    
    public AOTRawScriptInfo$Exception () {
        FoundationClassParser parser = new FoundationClassParser("System/Lang/Exception.jul");
        ainfo = new LazyAstInfo(parser, "System/Lang/Exception.jul", null);
        
        m_classes = new ArrayList<RawClassInfo>();
        m_classes.add(new RawClassInfo("BadSyntaxException", new AOTClassDeclInfo_BadSyntaxException (this)));
        m_classes.add(new RawClassInfo("RuntimeCheckException", new AOTClassDeclInfo_RuntimeCheckException (this)));
        m_classes.add(new RawClassInfo("DuplicateSymbolException", new AOTClassDeclInfo_DuplicateSymbolException (this)));
        m_classes.add(new RawClassInfo("IllegalOperandException", new AOTClassDeclInfo_IllegalOperandException (this)));
        m_classes.add(new RawClassInfo("IllegalLiteralException", new AOTClassDeclInfo_IllegalLiteralException (this)));
        m_classes.add(new RawClassInfo("UndefinedSymbolException", new AOTClassDeclInfo_UndefinedSymbolException (this)));
        m_classes.add(new RawClassInfo("NamespaceConflictException", new AOTClassDeclInfo_NamespaceConflictException (this)));
        m_classes.add(new RawClassInfo("CyclicDependencyException", new AOTClassDeclInfo_CyclicDependencyException (this)));
        m_classes.add(new RawClassInfo("IllegalBindingException", new AOTClassDeclInfo_IllegalBindingException (this)));

        m_requirements = new ArrayList<RequirementInfo>();
        m_requirements.add(new RequirementInfo("System", null));
    }

    public String getModuleName() {
        return "System.Lang";
    }

    public List<RequirementInfo> getRequirements() {
        return m_requirements;
    }

    public List<RawClassInfo> getClasses() {
        return m_classes;
    }

    public String getScriptFilePath() {
        return "System/Lang/Exception.jul";
    }
    
    // Types declared in this script //
    
        
    class AOTClassDeclInfo_BadSyntaxException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_BadSyntaxException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("Exception"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.BadSyntaxException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "BadSyntaxException";
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
    
        
    class AOTClassDeclInfo_RuntimeCheckException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_RuntimeCheckException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("Exception"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.RuntimeCheckException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "RuntimeCheckException";
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
    
        
    class AOTClassDeclInfo_DuplicateSymbolException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_DuplicateSymbolException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("BadSyntaxException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.DuplicateSymbolException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "DuplicateSymbolException";
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
    
        
    class AOTClassDeclInfo_IllegalOperandException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_IllegalOperandException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("RuntimeCheckException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.IllegalOperandException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "IllegalOperandException";
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
    
        
    class AOTClassDeclInfo_IllegalLiteralException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_IllegalLiteralException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("BadSyntaxException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.IllegalLiteralException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "IllegalLiteralException";
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
    
        
    class AOTClassDeclInfo_UndefinedSymbolException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_UndefinedSymbolException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("BadSyntaxException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.UndefinedSymbolException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "UndefinedSymbolException";
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
    
        
    class AOTClassDeclInfo_NamespaceConflictException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_NamespaceConflictException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("RuntimeCheckException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.NamespaceConflictException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "NamespaceConflictException";
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
    
        
    class AOTClassDeclInfo_CyclicDependencyException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_CyclicDependencyException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("RuntimeCheckException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.CyclicDependencyException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "CyclicDependencyException";
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
    
        
    class AOTClassDeclInfo_IllegalBindingException extends LazyClassDeclInfo {

        public AOTClassDeclInfo_IllegalBindingException(RawScriptInfo minfo) {
            super(minfo);
            
            m_parentNames.add(ParsedTypeName.makeFromFullName("RuntimeCheckException"));
        }

        private List<ParsedTypeName> m_parentNames = new ArrayList<ParsedTypeName>();
        private FQName m_fullName = new FQName("System.Lang.IllegalBindingException");
        
        public List<ParsedTypeName> getParentTypes(){
            return m_parentNames;
        }
        
        public FQName getFQName() {
            return m_fullName;
        }
        
        public String getName(){
            return "IllegalBindingException";
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