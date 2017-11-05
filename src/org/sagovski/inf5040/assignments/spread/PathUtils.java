package org.sagovski.inf5040.assignments.spread;

import java.io.File;

public final class PathUtils {

	public static String getAbsolutePath(final String relativePathToProjectRoot) {
		String rootPath = new File(".").getAbsolutePath();
		return (rootPath + File.separator + relativePathToProjectRoot);
	}

}
