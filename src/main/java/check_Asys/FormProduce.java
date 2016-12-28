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
 * FormProduce 产生对账结果的excel表格,该类定义了导出excel表的具体格式
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
	 * CreateForm 生成表格
	 * @param exportlist 生成表格的种类，种类的多少决定了一个excel表中的sheet的多少，exportlist对应sheet
	 * @param work_id 代理商id
	 * @param usetype 使用该函数的用户的类型
	 * @param dirpath 生成的excel表保存的路径
	 * @param filename 生成excel表的文件名
	 * @author zhangxinming
	 */
	public void  CreateForm(List<String> exportlist,String work_id,String usetype,String dirpath,String filename) {
		
		File dir = anyFile_Op.CreateDir(dirpath);//创建保存目录
		File file = anyFile_Op.CreateFile(dirpath,filename);//创建保存文件
		
		excel_rw.wXssfWorkbook = new XSSFWorkbook();
		ExcelFormAtr formatr = new ExcelFormAtr(dirpath,filename,null);
		for (int i = 0; i < exportlist.size(); i++) {
			if (exportlist.get(i).equals("export_totalori")) {
				formatr.sheetname = "货款表";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_TotalOri(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_TruePayNoBInput")) {
				formatr.sheetname = "无法关联的真实付款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_TruePayNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_FalsePayNoBInput")) {
				formatr.sheetname = "无法关联的无用付款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_FalsePayNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_PayHasBinput")) {
				formatr.sheetname = "关联到出纳的付款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_PayHasBinput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_OriorderHasBInput")) {
				formatr.sheetname = "收到汇款的货款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_OriorderHasBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_OriorderNoBInput")) {
				formatr.sheetname = "没有收到汇款的货款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_OriorderNoBInput(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputToClient")) {
				formatr.sheetname = "关联到客户的出纳记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_BInputToClient(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputFailConnect")) {
				formatr.sheetname = "无法关联的汇款记录";
				OwerAtr owerAtr = new OwerAtr(work_id, usetype);
				CreateForm_BInputFailConnect(formatr,owerAtr,file,i);
			}
			else if (exportlist.get(i).equals("export_BInputToContract")) {
				formatr.sheetname = "关联到合同的出纳记录";
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
	 * CreateForm_TotalOri 对账结束后的货款表 
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_TotalOri(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){//应该是创建一个sheet的功能

		Sheet totaloriSheet = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(totaloriSheet, new Export_TotalOriForm().formhead);//设置表头
		
		List<OriOrder> list_at = null;
		if (owerAtr.userType.equals("bu")) {//普通对账人员
			list_at = dao_List.tDao.FindBySpeElement_S("owner", owerAtr.work_id);
		}
		else {
			logger.error("对账人员的身份错误");
		}
		
		excel_rw.SetTotalAFormBody(list_at, totaloriSheet);//设置表体
	}
	
	/**
	 * CreateForm_TruePayNoBInput 没有关联到出纳信息的真实手机付款记录
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_TruePayNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){//应该是创建一个sheet的功能

		Sheet nobinputpay = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(nobinputpay, new Export_PayRecord().formhead);//设置表头
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_TruePayNoBInput(owerAtr.work_id),nobinputpay);//设置表体
		
	}
	
	/**
	 * CreateForm_FalsePayNoBInput 没有关联到出纳信息的无用手机付款记录
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_FalsePayNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet FalsePayNoBInput = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(FalsePayNoBInput, new Export_PayRecord().formhead);//设置表头
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_FalsePayNoBInput(owerAtr.work_id),FalsePayNoBInput);//设置表体
	}
	
	/**
	 * CreateForm_PayHasBinput 关联到出纳信息的手机付款记录
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_PayHasBinput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet nobinputpay = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(nobinputpay, new Export_PayRecord().formhead);//设置表头
		
		excel_rw.SetPayPecordFormBody(cARseult.Produce_PayHasBInput(owerAtr.work_id),nobinputpay);//设置表体
	}
	
	/**
	 * CreateForm_OriorderHasBInput 本月有收到付款的货款记录
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_OriorderHasBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet OriorderHasBInpu = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(OriorderHasBInpu, new Export_TotalOriForm().formhead);//设置表头
		
		excel_rw.SetTotalAFormBody(cARseult.Produce_OriorderHasBInput(owerAtr.work_id),OriorderHasBInpu);//设置表体
	}
	
	/**
	 * CreateForm_OriorderNoBInput 本月没有收到付款的货款记录
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_OriorderNoBInput(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet OriorderNoBInput = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(OriorderNoBInput, new Export_TotalOriForm().formhead);//设置表头
		
		excel_rw.SetTotalAFormBody(cARseult.Produce_OriorderNoBInput(owerAtr.work_id),OriorderNoBInput);//设置表体
	}
	
	/**
	 * CreateForm_BInputToClient 出纳关联到客户的结果
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_BInputToClient(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputToClient = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputToClient, new Export_Binput().formhead);//设置表头
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputToClient(owerAtr.work_id),BInputToClient);//设置表体
	}
	
	/**
	 * CreateForm_BInputFailConnect 出纳无法关联到合同或者客户结果
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_BInputFailConnect(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputFailConnect = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputFailConnect, new Export_Binput().formhead);//设置表头
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputFailConnect(owerAtr.work_id),BInputFailConnect);//设置表体
	}
	
	/**
	 * CreateForm_BInputToContract 出纳关联到合同的结果
	 * @param formatr excel的属性
	 * @param owerAtr 生成excel的用户属性
	 * @param file 保存的excel文件
	 * @param i excel 中sheet的位置
	 */
	public void CreateForm_BInputToContract(ExcelFormAtr formatr,OwerAtr owerAtr,File file,int i){
		Sheet BInputToContract = excel_rw.wXssfWorkbook.createSheet();
		excel_rw.wXssfWorkbook.setSheetName(i, formatr.sheetname);
		
		excel_rw.SetFormHead(BInputToContract, new Export_Binput().formhead);//设置表头
		
		excel_rw.SetBinputFormBody(cARseult.Produce_BInputToContract(owerAtr.work_id),BInputToContract);//设置表体
	}
	
	/**
	 * ExcelFormAtr 生成excel表的属性
	 * @author zhangxinming
	 *
	 */
	public class ExcelFormAtr{
		public String dirname;//保存路径
		public String filename;//文件名
		public String sheetname;//sheet的名字
		public ExcelFormAtr(String dirname,String filename,String sheetname) {
			this.dirname = dirname;
			this.filename = filename;
			this.sheetname = sheetname;
		}
	}
	
	/**
	 * OwerAtr 生成者的属性
	 * @author zhangxinming
	 *
	 */
	public class OwerAtr{
		public 	String work_id;//用户id
		public String userType;//用户类型
		public OwerAtr (String work_id,String userType) {
			this.work_id = work_id;
			this.userType = userType;
		}
	}
	
	/**
	 * Export_TotalOriForm 货款类excel的表头定义
	 * @author zhangxinming
	 *
	 */
	public class Export_TotalOriForm{
/*		public final static int order_num = 0;
		public final static int input = 1;
		public final static int debt = 2;
		public final static int total = 3;
		public final static int client = 4;*/
/*		public final static int num = 0;//序号
		public final static int province = 1;//省份
		public final static int clientName = 2;//客户名称
		public final static int cusCompanyid = 3;//客户id
		public final static int dingdanNum = 4;//订单号
		public final static int productName = 5;//产品名称
		public final static int facilityName = 6;//设备名称
		public final static int contract = 7;//合同号
		public final static int productTime = 8;//发货时间
		public final static int paymentOwner = 9;//货款主体
		public final static int paymentNature = 10;//货款性质
		public final static int inputNature = 11;//回款性质
		public final static int totalMoney  =12;//合同总额
		public final static int debetMoney  = 13;//在外金额
		public final static int actualPayer  = 14;//实际付款人
		public final static int payway  = 15;//付款方式
		public final static int inputMonth = 16;//本月回款
		public final static int inputClient = 17;//客户回款*/
		
		public final static int num = 0;//序号
		public final static int  province = 1;//省份
		public final static int clientName = 2;//客户名称
		public final static int cusCompanyid = 3;//客户id
		public final static int paymentNature = 4;//货款性质
		public final static int totalMoney = 5;//合同总额
		public final static int debetMoney = 6;//在外金额
		public final static int inputMonth = 7;//本月回款
		
		public List<String> formhead;
		public Export_TotalOriForm(){
			formhead = new ArrayList<String>();
			formhead.add(num,"序号");
			formhead.add(province,"省份");
			formhead.add(clientName, "客户名称/按揭借款人");
			formhead.add(cusCompanyid, "合同买受人客户码");
			formhead.add(paymentNature, "货款性质");
			formhead.add(totalMoney, "合同总额");
			formhead.add(debetMoney, "在外货款总额");
			formhead.add(inputMonth, "本月回款");
		}
	}
	
	/**
	 * Export_PayRecord 付款类excel的表头定义
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
			formhead.add(payer, "付款人");
			formhead.add(paymoney, "付款金额");
			formhead.add(payway, "付款方式");
			formhead.add(receiver, "款项接收人");
			formhead.add(connectperson, "对账联系人");
		}
	}
	
	/**
	 * Export_Binput 出纳类的表头定义
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
			formhead.add(payee, "收款账号名称");
			formhead.add(paymoney, "收款金额");
			formhead.add(payway, "收款方式");
			formhead.add(payer, "付款账户名称");
		}
	}
	
}
