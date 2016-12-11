package file_op;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import en_de_code.ED_Code;

/**
 * AnyFile_Op �ļ��ĳ��ò���:��д�ļ�������Ŀ¼��
 * @author zhangxinming
 * @version 1.0.0
 */
public class AnyFile_Op{

	public byte [] ReadFile(MultipartFile mfile){
		if (mfile.getOriginalFilename().equals("")) {
			return null;
		}
		
		long filesize = mfile.getSize();
		byte read_b[] = new byte[(int)filesize];
        /*��ȡ�ϴ����ļ�������*/
		InputStream inputStream;
		try {
			inputStream = mfile.getInputStream();
			inputStream.read(read_b); 
			inputStream.close();
			System.out.println(ED_Code.ByteArray_Act_Len(read_b));
		/*��ȡ�ϴ����ļ�������*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return read_b;
	}
	
	public byte [] ReadFileToStream(MultipartFile mfile){
		if (mfile.getOriginalFilename().equals("")) {
			return null;
		}
		
		long filesize = mfile.getSize();
		byte read_b[] = new byte[(int)filesize];
        /*��ȡ�ϴ����ļ�������*/
		InputStream inputStream;
		try {
			inputStream = mfile.getInputStream();
			inputStream.read(read_b); 
			inputStream.close();
			System.out.println(ED_Code.ByteArray_Act_Len(read_b));
		/*��ȡ�ϴ����ļ�������*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return read_b;
	}
	
	public boolean WriteFile(AnyFileElement aF_Element,byte []read_b,File file){
		FileOutputStream out = null;
		
		/*���ļ����浽������ָ��Ŀ¼��*/
		System.out.println(aF_Element.filename);
		try {
			out = new FileOutputStream(file);
			out.write(read_b,0,aF_Element.filesize);
			out.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		/*���ļ����浽������ָ��Ŀ¼��*/		
	}
	
	public  File CreateDir(String dirname) {
		/*�ж��ļ����Ƿ����*/
		File dir = new File(dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			System.out.println(dirname + "������");
			dir.mkdirs();
		}
		/*�ж��ļ����Ƿ����*/
		return dir;
	}
	
	public File CreateFile(String dirname,String filename) {
		/*�ж��ļ��Ƿ����*/
		boolean exitflag = true;
		File file = new File(dirname + "/" + filename);
		if (!file.exists()) {
			exitflag = false;
			System.out.println(filename + "don't exit"); 
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{//���´���,��linux���Ƿ����Ȩ������
			System.out.println(filename + "delet and create again");
			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*�ж��ļ��Ƿ����*/
		return file;
	}
	
	public class AnyFileElement{
		public String filename;
		public String dirname;
		public int filesize;

		public AnyFileElement(String filename,String dirname,int filesize) {
			this.filename = filename;
			this.dirname = dirname;
			this.filesize = filesize;
	}
		// TODO Auto-generated constructor stub
	}
}
