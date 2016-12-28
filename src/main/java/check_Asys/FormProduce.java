package check_Asys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SessionFactory;


import check_Asys.CheckAcManage.Dao_List;
import controller.Check_MainController;
import entity.OriOrder;
import file_op.AnyFile_Op;
import file_op.Excel_RW;
import sun.util.logging.resources.logging;

/**
 * FormProduce �������˽����excel���,���ඨ���˵���excel��ľ����ʽ
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class FormProduce {
	private static Logger logger = LogManager.getLogger(FormProduce.class);
	public SessionFactory wFactory;
	public AnyFile_Op anyFile_Op;
	public Excel_RW excel_rw;
	
	public Dao_List dao_List;
	public CheckARseult cARseult;
	
	public FormProduce(SessionFactory wFactory,Dao_List dao_List,CheckARseult cARseult){
		this.wFactory = wFactory;
		anyFile_Op = new AnyFile_Op();
		excel_rw = new Excel_RW();
		
		this.dao_List = dao_List;
		this.cARseult = cARseult;
	}
	
	/**
	 * CreateForm ���ɱ��
	 * @param exportlist ���ɱ������࣬����Ķ��پ�����һ��excel���е�sheet�Ķ��٣�exportlist��Ӧsheet
	 * @param work_id ������id
	 * @param usetype ʹ�øú������û�������
	 * @param dirpath ���ɵ�excel�����·��
	 * @param filename ����excel����ļ���
	 * @author zhangxinming
	 */
	public void  CreateForm(List<String> exportlist,String work_id,String usetype,String dirpath,String filename) {
		
		File dir = anyFile_Op.CreateDir(dirpath);//��������Ŀ¼
		File file = anyFile_Op.CreateFile(dirpath,filename);//���������ļ�
		
		excel_rw.wXssfWorkbook = new XSSFWorkbook();
		ExcelFormAtr formatr = new ExcelFormAtr(dirpath,filename,null);
		for (int i = 0; i < exportlist.size(); i++) {
			if (exportlist.get(i).equals("export_totalori")) {
				formatr.sheetname = "�����";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_TotalOri(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_TruePayNoBInput")) {
				formatr.sheetname = "�޷���������ʵ�����¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_TruePayNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_FalsePayNoBInput")) {
				formatr.sheetname = "�޷����������ø����¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_FalsePayNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_PayHasBinput")) {
				formatr.sheetname = "���������ɵĸ����¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_PayHasBinput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_OriorderHasBInput")) {
				formatr.sheetname = "�յ����Ļ����¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_OriorderHasBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_OriorderNoBInput")) {
				formatr.sheetname = "û���յ����Ļ����¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_OriorderNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputToClient")) {
				formatr.sheetname = "�������ͻ��ĳ��ɼ�¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_BInputToClient(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputFailConnect")) {
				formatr.sheetname = "�޷������Ļ���¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_BInputFailConnect(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputToContract")) {
				formatr.sheetname = "��������ͬ�ĳ��ɼ�¼";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_BInputToContract(formatr,owerAtr,file,i);
			}
			else{
				System.out.println("unknow export_type");
			}
		}
		
		try {
			FileOutputStream out;
			out = new FileOutputStream(file);
			excel_rw.wXssfWorkbook.write(out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * CreateForm_TotalOri ���˽�����Ļ���� 
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_TotalOri(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){//Ӧ���Ǵ���һ��sheet�Ĺ���

		Sheet totaloriSheet = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(totaloriSheet, new Export_TotalOriForm().formhead);//���ñ�ͷ
		
		List<OriOrder> list_at = null;
		if (owerAtr.userType.equals("bu")) {//��ͨ������Ա
			list_at = dao_List.tDao.FindBySpeElement_S("owner", owerAtr.work_id);
		}
		else {
			logger.error("������Ա����ݴ���");
		}
		
		excel_rw.SetTotalAFormBody(list_at, totaloriSheet);//���ñ���
	}
	
	/**
	 * CreateForm_TruePayNoBInput û�й�����������Ϣ����ʵ�ֻ������¼
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_TruePayNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){//Ӧ���Ǵ���һ��sheet�Ĺ���

		Sheet nobinputpay = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(nobinputpay, new Export_PayRecord().formhead);//���ñ�ͷ
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_TruePayNoBInput(owerAtr.work_id),nobinputpay);//���ñ���
		
	}
	
	/**
	 * CreateForm_FalsePayNoBInput û�й�����������Ϣ�������ֻ������¼
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_FalsePayNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet FalsePayNoBInput = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(FalsePayNoBInput, new Export_PayRecord().formhead);//���ñ�ͷ
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_FalsePayNoBInput(owerAtr.work_id),FalsePayNoBInput);//���ñ���
	}
	
	/**
	 * CreateForm_PayHasBinput ������������Ϣ���ֻ������¼
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_PayHasBinput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet nobinputpay = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(nobinputpay, new Export_PayRecord().formhead);//���ñ�ͷ
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_PayHasBInput(owerAtr.work_id),nobinputpay);//���ñ���
	}
	
	/**
	 * CreateForm_OriorderHasBInput �������յ�����Ļ����¼
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_OriorderHasBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet OriorderHasBInpu = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(OriorderHasBInpu, new Export_TotalOriForm().formhead);//���ñ�ͷ
		
		excel_rw.SetTotalAFormBody(cARseult.Produce_OriorderHasBInput(owerAtr.work_id),OriorderHasBInpu);//���ñ���
	}
	
	/**
	 * CreateForm_OriorderNoBInput ����û���յ�����Ļ����¼
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_OriorderNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet OriorderNoBInput = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(OriorderNoBInput, new Export_TotalOriForm().formhead);//���ñ�ͷ
		
		excel_rw.SetTotalAFormBody(cARseult.Produce_OriorderNoBInput(owerAtr.work_id),OriorderNoBInput);//���ñ���
	}
	
	/**
	 * CreateForm_BInputToClient ���ɹ������ͻ��Ľ��
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_BInputToClient(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputToClient = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputToClient, new Export_Binput().formhead);//���ñ�ͷ
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputToClient(owerAtr.work_id),BInputToClient);//���ñ���
	}
	
	/**
	 * CreateForm_BInputFailConnect �����޷���������ͬ���߿ͻ����
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_BInputFailConnect(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputFailConnect = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputFailConnect, new Export_Binput().formhead);//���ñ�ͷ
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputFailConnect(owerAtr.work_id),BInputFailConnect);//���ñ���
	}
	
	/**
	 * CreateForm_BInputToContract ���ɹ�������ͬ�Ľ��
	 * @param formatr excel������
	 * @param owerAtr ����excel���û�����
	 * @param file �����excel�ļ�
	 * @param i excel ��sheet��λ��
	 */
	public void CreateForm_BInputToContract(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputToContract = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputToContract, new Export_Binput().formhead);//���ñ�ͷ
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputToContract(owerAtr.work_id),BInputToContract);//���ñ���
	}
	
	/**
	 * ExcelFormAtr ����excel�������
	 * @author zhangxinming
	 *
	 */
	public class ExcelFormAtr{
		public String dirname;//����·��
		public String filename;//�ļ���
		public String sheetname;//sheet������
		public ExcelFormAtr(String dirname,String filename,String sheetname) {
			this.dirname = dirname;
			this.filename = filename;
			this.sheetname = sheetname;
		}
	}
	
	/**
	 * OwerAtr �����ߵ�����
	 * @author zhangxinming
	 *
	 */
	public class OwerAtr{
		public 	String work_id;//�û�id
		public String userType;//�û�����
		public OwerAtr (String work_id,String userType) {
			this.work_id = work_id;
			this.userType = userType;
		}
	}
	
	/**
	 * Export_TotalOriForm ������excel�ı�ͷ����
	 * @author zhangxinming
	 *
	 */
	public class Export_TotalOriForm{
/*		public final static int order_num = 0;
		public final static int input = 1;
		public final static int debt = 2;
		public final static int total = 3;
		public final static int client = 4;*/
/*		public final static int num = 0;//���
		public final static int province = 1;//ʡ��
		public final static int clientName = 2;//�ͻ�����
		public final static int cusCompanyid = 3;//�ͻ�id
		public final static int dingdanNum = 4;//������
		public final static int productName = 5;//��Ʒ����
		public final static int facilityName = 6;//�豸����
		public final static int contract = 7;//��ͬ��
		public final static int productTime = 8;//����ʱ��
		public final static int paymentOwner = 9;//��������
		public final static int paymentNature = 10;//��������
		public final static int inputNature = 11;//�ؿ�����
		public final static int totalMoney  =12;//��ͬ�ܶ�
		public final static int debetMoney  = 13;//������
		public final static int actualPayer  = 14;//ʵ�ʸ�����
		public final static int payway  = 15;//���ʽ
		public final static int inputMonth = 16;//���»ؿ�
		public final static int inputClient = 17;//�ͻ��ؿ�*/
		
		public final static int num = 0;//���
		public final static int  province = 1;//ʡ��
		public final static int clientName = 2;//�ͻ�����
		public final static int cusCompanyid = 3;//�ͻ�id
		public final static int paymentNature = 4;//��������
		public final static int totalMoney = 5;//��ͬ�ܶ�
		public final static int debetMoney = 6;//������
		public final static int inputMonth = 7;//���»ؿ�
		
		public List<String> formhead;
		public Export_TotalOriForm(){
			formhead = new ArrayList<String>();
			formhead.add(num,"���");
			formhead.add(province,"ʡ��");
			formhead.add(clientName, "�ͻ�����/���ҽ����");
			formhead.add(cusCompanyid, "��ͬ�����˿ͻ���");
			formhead.add(paymentNature, "��������");
			formhead.add(totalMoney, "��ͬ�ܶ�");
			formhead.add(debetMoney, "��������ܶ�");
			formhead.add(inputMonth, "���»ؿ�");
		}
	}
	
	/**
	 * Export_PayRecord ������excel�ı�ͷ����
	 * @author zhangxinming
	 *
	 */
	public class Export_PayRecord{
		public final static int payer = 0;
		public final static int paymoney = 1;
		public final static int payway = 2;
		public final static int receiver = 3;
		public final static int connectperson = 4;
		
		public List<String> formhead;
		public Export_PayRecord(){
			formhead = new ArrayList<String>();
			formhead.add(payer, "������");
			formhead.add(paymoney, "������");
			formhead.add(payway, "���ʽ");
			formhead.add(receiver, "���������");
			formhead.add(connectperson, "������ϵ��");
		}
	}
	
	/**
	 * Export_Binput ������ı�ͷ����
	 * @author zhangxinming
	 *
	 */
	public class Export_Binput{
		public final static int payee = 0;
		public final static int paymoney = 1;
		public final static int payway = 2;
		public final static int payer = 3;
		
		public List<String> formhead;
		public Export_Binput(){
			formhead = new ArrayList<String>();
			formhead.add(payee, "�տ��˺�����");
			formhead.add(paymoney, "�տ���");
			formhead.add(payway, "�տʽ");
			formhead.add(payer, "�����˻�����");
		}
	}
	
}
