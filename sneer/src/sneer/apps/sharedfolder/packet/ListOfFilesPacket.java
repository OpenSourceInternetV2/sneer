package sneer.apps.sharedfolder.packet;

import wheel.io.files.impl.FileInfo;

public class ListOfFilesPacket implements SharedFolderPacket{

	public final FileInfo[] _infos;

	public ListOfFilesPacket(FileInfo[] files){
		_infos = files;
	}
	
	public int type() {
		return LIST_OF_FILES;
	}

	private static final long serialVersionUID = 1L;
}
