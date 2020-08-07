package info.julang.ide.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import info.julang.parser.LazyAstInfo;

/**
 * Stores all ASTs built out of each file.
 * 
 * @author Ming Zhou
 */
public class ASTRepository {

	private Map<IProject, Map<IFile, LazyAstInfo>> map;
	
	public ASTRepository() { 
		map = new HashMap<IProject, Map<IFile, LazyAstInfo>>();
	}
	
	public synchronized void put(IFile file, LazyAstInfo info) {
		if (info != null) {
			IProject proj = file.getProject();
			Map<IFile, LazyAstInfo> files = map.get(proj);
			if (files == null) {
				files = new HashMap<IFile, LazyAstInfo>();
				map.put(proj, files);
			}
			
			files.put(file, info);
		}
	}

	public synchronized void removeAll(List<IFile> filesRemoved) {
		for (IFile file : filesRemoved) {
			IProject proj = file.getProject();
			Map<IFile, LazyAstInfo> files = map.get(proj);
			if (files != null) {
				files.remove(file);
			}
		}
	}
	
	public synchronized void removeAllExcept(Set<IFile> filesKept) {
		List<IFile> filesToRemove = new ArrayList<>();
		filesKept.stream().findAny().ifPresent(file -> {
			IProject proj = file.getProject();
			Map<IFile, LazyAstInfo> files = map.get(proj);
			
			for (IFile f : files.keySet()) {
				if (!filesKept.contains(f)) {
					filesToRemove.add(f);
				}
			}
		});
		
		if (filesToRemove.size() > 0) {
			removeAll(filesToRemove);
		}
	}
	
	public LazyAstInfo get(IFile file) {
		Map<IFile, LazyAstInfo> files = map.get(file.getProject());
		if (files != null) {
			return files.get(file);
		}
		
		return null;
	}

	public boolean isEnabledFor(IProject proj) {
		return map.get(proj) != null;
	}
}
