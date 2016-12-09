package check_Asys;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import dao.OpLog_Dao;
import entity.OpLog;

/**
 * OpLog_Service ������־�� �ṩ�û��Ĳ�����־����
 * @author zhangxinming
 *
 */
public class OpLog_Service {
	
	private static Logger logger = LogManager.getLogger(OpLog_Service.class);
	public OpLog_Dao opLog_Dao;
	
	/*��־����*/
	public static final String Log = "��¼";
	public static final String SIGNOUT = "ע��";
	public static final String Upload_Pay = "�ϴ�������Ϣ";
	public static final String Upload_Pay_Wexin = "΢���ϴ�������Ϣ";
	public static final String IMPORT = "�����������ɱ�";
	public static final String START_CHECKWORK = "ִ�ж���";
	public static final String ENTRER_CaModel = "�����������";
	public static final String CANCEL_CaAgain = "ȡ�������¶���";
	public static final String EXPORT_CARes = "�������˽��";
	public static final String Update_Pay_Weixin = "����΢�Ÿ�����Ϣ";
	/*��־����*/
	
	/*�û�����*/
	public static final String utype_as = "�����̲���";
	public static final String utype_cp = "�ͻ�";
	public static final String utype_ma = "����Ա";
	public static final String utype_am = "�����̹���Ա";
	public static final String utype_un = "δ֪�û�����";
	/*�û�����*/
	
	/*�������*/
	public static final String result_success = "�ɹ�";
	public static final String result_failed = "ʧ��";
	/*�������*/
	
	public OpLog_Service(SessionFactory wFactory){
		opLog_Dao = new OpLog_Dao(wFactory);
	}
	
	private String produce_time(){
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
		String dates = sFormat.format(date);
		
		return dates;
	}
	
	/**
	 * AddLog ����һ����־
	 * @param usertype ��־������û�����
	 * @param who �û���
	 * @param content ��־����
	 * @param result �������
	 * @author zhangxinming
	 */
	public void AddLog(String usertype,String who,String content,String result){
		String op_time = produce_time();
		
		OpLog logrd = new OpLog();
		logrd.setTime(op_time);
		logrd.setUsertype(usertype);
		logrd.setUsername(who);
		logrd.setContent(content);
		logrd.setResult(result);
		
		opLog_Dao.add(logrd);
		logger.info("���һ����־��¼" + usertype + ":" + who + ":" + content + ":" + result);
	}
}
