package info.julang.typesystem.jclass.jufc;

import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.hosting.HostedMethodProviderFactory;

import java.util.Map;

/**
 * This source file is automatically generated.
 */
public class SystemRawScriptInfoInitializer {

    public static HostedMethodProviderFactory[] getAllFactories(){
       return new HostedMethodProviderFactory[]{
           info.julang.typesystem.jclass.jufc.System.Collection.JList.Factory,
           info.julang.typesystem.jclass.jufc.System.Collection.JMap.Factory,
           info.julang.typesystem.jclass.jufc.System.Collection.JQueue.Factory,
           info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptLock.Factory,
           info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread.Factory,
           info.julang.typesystem.jclass.jufc.System.DateTime.Factory,
           info.julang.typesystem.jclass.jufc.System.IO.JDirectory.Factory,
           info.julang.typesystem.jclass.jufc.System.IO.JFile.Factory,
           info.julang.typesystem.jclass.jufc.System.IO.JFileStream.Factory,
           info.julang.typesystem.jclass.jufc.System.JConsole.Factory,
           info.julang.typesystem.jclass.jufc.System.JEnvironment.Factory,
           info.julang.typesystem.jclass.jufc.System.JProcess.Factory,
           info.julang.typesystem.jclass.jufc.System.Network.JNetAddress.Factory,
           info.julang.typesystem.jclass.jufc.System.Network.ScriptServerSocket.Factory,
           info.julang.typesystem.jclass.jufc.System.Network.ScriptSocket.Factory,
           info.julang.typesystem.jclass.jufc.System.Network.ScriptSocketStream.Factory,
           info.julang.typesystem.jclass.jufc.System.ProcessPipeStream.Factory,
           info.julang.typesystem.jclass.jufc.System.Reflection.ScriptCtor.Factory,
           info.julang.typesystem.jclass.jufc.System.Reflection.ScriptField.Factory,
           info.julang.typesystem.jclass.jufc.System.Reflection.ScriptMethod.Factory,
           info.julang.typesystem.jclass.jufc.System.Reflection.ScriptParam.Factory,
           info.julang.typesystem.jclass.jufc.System.Reflection.ScriptScript.Factory,
           info.julang.typesystem.jclass.jufc.System.ScriptType.Factory,
           info.julang.typesystem.jclass.jufc.System.Util.JMath.Factory,
           info.julang.typesystem.jclass.jufc.System.Util.JRegex.Factory,
           info.julang.typesystem.jclass.jufc.System.Util.Match.Factory,
       };
    }

    static void initializeFileLocations(Map<String, String[]> map){
		map.put("System.Concurrency", new String[]{
			"Lock",
			"Promise",
			"Thread",
		});
		map.put("System.Collection", new String[]{
			"Container",
			"Exception",
			"List",
			"Map",
			"Queue",
		});
		map.put("System.Lang", new String[]{
			"Exception",
		});
		map.put("System.Reflection", new String[]{
			"Constructor",
			"Exception",
			"Field",
			"Member",
			"Method",
			"Parameter",
			"Script",
		});
		map.put("System.Util", new String[]{
			"Exception",
			"Interfaces",
			"Internals",
			"Math",
			"Regex",
		});
		map.put("System.IO", new String[]{
			"Directory",
			"Exception",
			"File",
			"FileStream",
			"Item",
			"Stream",
		});
		map.put("System", new String[]{
			"Annotation",
			"Console",
			"Environment",
			"Exception",
			"HOLI",
			"PlatformObject",
			"Process",
			"Time",
			"Type",
		});
		map.put("System.Network", new String[]{
			"Address",
			"Exception",
			"ISocket",
			"ServerSocket",
			"Socket",
			"SocketStream",
		});
    }
    
    static void initialize(Map<String, SystemRawScriptInfoFactory<? extends RawScriptInfo>> map){
        
        map.put("System/Annotation.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Annotation>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Annotation.class));
        
        map.put("System/Collection/Container.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Container>(
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Container.class));
        
        map.put("System/Collection/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Exception.class));
        
        map.put("System/Collection/List.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$List>(
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$List.class));
        
        map.put("System/Collection/Map.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Map>(
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Map.class));
        
        map.put("System/Collection/Queue.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Queue>(
            info.julang.typesystem.jclass.jufc.System.Collection.AOTRawScriptInfo$Queue.class));
        
        map.put("System/Concurrency/Lock.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Lock>(
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Lock.class));
        
        map.put("System/Concurrency/Promise.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Promise>(
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Promise.class));
        
        map.put("System/Concurrency/Thread.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Thread>(
            info.julang.typesystem.jclass.jufc.System.Concurrency.AOTRawScriptInfo$Thread.class));
        
        map.put("System/Console.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Console>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Console.class));
        
        map.put("System/Environment.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Environment>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Environment.class));
        
        map.put("System/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Exception.class));
        
        map.put("System/HOLI.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$HOLI>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$HOLI.class));
        
        map.put("System/IO/Directory.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Directory>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Directory.class));
        
        map.put("System/IO/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Exception.class));
        
        map.put("System/IO/File.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$File>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$File.class));
        
        map.put("System/IO/FileStream.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$FileStream>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$FileStream.class));
        
        map.put("System/IO/Item.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Item>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Item.class));
        
        map.put("System/IO/Stream.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Stream>(
            info.julang.typesystem.jclass.jufc.System.IO.AOTRawScriptInfo$Stream.class));
        
        map.put("System/Lang/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Lang.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.Lang.AOTRawScriptInfo$Exception.class));
        
        map.put("System/Network/Address.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Address>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Address.class));
        
        map.put("System/Network/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Exception.class));
        
        map.put("System/Network/ISocket.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$ISocket>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$ISocket.class));
        
        map.put("System/Network/ServerSocket.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$ServerSocket>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$ServerSocket.class));
        
        map.put("System/Network/Socket.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Socket>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$Socket.class));
        
        map.put("System/Network/SocketStream.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$SocketStream>(
            info.julang.typesystem.jclass.jufc.System.Network.AOTRawScriptInfo$SocketStream.class));
        
        map.put("System/PlatformObject.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$PlatformObject>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$PlatformObject.class));
        
        map.put("System/Process.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Process>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Process.class));
        
        map.put("System/Reflection/Constructor.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Constructor>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Constructor.class));
        
        map.put("System/Reflection/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Exception.class));
        
        map.put("System/Reflection/Field.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Field>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Field.class));
        
        map.put("System/Reflection/Member.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Member>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Member.class));
        
        map.put("System/Reflection/Method.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Method>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Method.class));
        
        map.put("System/Reflection/Parameter.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Parameter>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Parameter.class));
        
        map.put("System/Reflection/Script.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Script>(
            info.julang.typesystem.jclass.jufc.System.Reflection.AOTRawScriptInfo$Script.class));
        
        map.put("System/Time.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Time>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Time.class));
        
        map.put("System/Type.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Type>(
            info.julang.typesystem.jclass.jufc.System.AOTRawScriptInfo$Type.class));
        
        map.put("System/Util/Exception.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Exception>(
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Exception.class));
        
        map.put("System/Util/Interfaces.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Interfaces>(
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Interfaces.class));
        
        map.put("System/Util/Internals.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Internals>(
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Internals.class));
        
        map.put("System/Util/Math.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Math>(
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Math.class));
        
        map.put("System/Util/Regex.jul", new SystemRawScriptInfoFactory<
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Regex>(
            info.julang.typesystem.jclass.jufc.System.Util.AOTRawScriptInfo$Regex.class));
    }
    
}
