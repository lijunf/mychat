package com.lucien.xtest;

import java.io.File;
import java.io.IOException;

public class RenameIMG {

	public static void main(String[] argv) throws IOException {

		for (int i = 1; i < 52; i++) {
			 File f = new File("C:\\Users\\Administrator\\Desktop\\chat\\expression\\monkey\\"+i+"[1].gif"); 

			 f.renameTo(new File("C:\\Users\\Administrator\\Desktop\\chat\\expression\\monkey\\"+i+".gif"));
		}
	}
}
