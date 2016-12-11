package file_op;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.SessionFactory;

import com.mysql.fabric.xmlrpc.base.Data;
import com.sun.org.apache.bcel.internal.generic.ReturnaddressType;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import antlr.collections.List;
import check_Asys.FormProduce.Export_Binput;
import check_Asys.FormProduce.Export_PayRecord;
import check_Asys.FormProduce.Export_TotalOriForm;
import controller.FormManagerController.ExcelFileAtr;
import controller.FormManagerController.OwerAtr;
import dao.Total_Account_Dao;
import entity.Agent;
import entity.BankInput;
import entity.OriOrder;
import entity.OriOrderId;
import entity.PayRecord;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Excel_RW ��дexcel�Ľӿ�
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class Excel_RW {
	private static Logger logger = LogManager.getLogger(Excel_RW.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public Sheet wsheet;
	public Sheet rsheet;
	
	public XSSFWorkbook wXssfWorkbook;
	public XSSFWorkbook rXssfWorkbook;
	
	public  Excel_RW() {
		// TODO Auto-generated constructor stub
//		table = new ArrayList<Excel_Row>();
	//	wXssfWorkbook = new XSSFWorkbook();
	}	
	
	public  Sheet CreateWSheet(String sheetname){
		Sheet sheet = wXssfWorkbook.createSheet(sheetname);
		return sheet;
	}
	
	public static java.util.List<String> WriteExcel(ExcelFileAtr eAtr,OwerAtr oAtr,SessionFactory wFactory){
		
		String dirname = eAtr.dirname;
		String filename = eAtr.filename;
		String sheetname = eAtr.sheetname;
		
		String work_id = oAtr.work_id;
		String userType = oAtr.userType;
		
		/*��������Ŀ¼*/
		File dir = CreateDir(dirname);
		/*��������Ŀ¼*/

		File file = CreateFile(dirname,filename);
		
		/*����sheet*/
		FileOutputStream out = null;
		Sheet sheet1 = null;
		XSSFWorkbook workbook = null;
		try {	
			 workbook = new XSSFWorkbook();	
			 sheet1 = workbook.createSheet(sheetname);
			/*���ñ������*/
			/*���ñ�ͷ*/
			SetTotalAHead(workbook,sheet1);
			/*���ñ�ͷ*/
			
			/*���ñ��������*/
			java.util.List list_at = ChooseAccout(userType,work_id,wFactory);//�����û����ͣ�ѡ����Ӧ�ı���
			
			for(int i = 0;i<list_at.size();i++){
				Row row_body = sheet1.createRow(i+1);
				OriOrder order = (OriOrder) list_at.get(i);
				
				/*���ö�Ӧ�������*/
				SetTotalABody(row_body,order,workbook);
				/*���ö�Ӧ�������*/
			}
			/*���ñ��������*/
			/*���ñ������*/
			
			
			out = new FileOutputStream(file);
			workbook.write(out);

		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����sheet*/
		
		java.util.List<String> result = new ArrayList<String>();
		result.add("���˵�");
		result.add(CreateLink("��������", filename));
		return result;
	}
	
	public static File CreateDir(String dirname) {
		/*�ж��ļ����Ƿ����*/
		File dir = new File(dirname);
		if (!dir.exists() && !dir.isDirectory()) {
			System.out.println(dirname + "������");
			dir.mkdir();
		}
		/*�ж��ļ����Ƿ����*/
		
		return dir;
	}
	
	public static File CreateFile(String dirname,String filename) {
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
	
	public static void SetCellStyle(XSSFWorkbook workbook,Cell cell){
		/*���õ�Ԫ��ʽ*/
		CellStyle tStyle = workbook.createCellStyle();
		tStyle.setAlignment(CellStyle.ALIGN_CENTER);
		tStyle.setBorderBottom(CellStyle.BORDER_THIN);
		tStyle.setBorderLeft(CellStyle.BORDER_THIN);
		tStyle.setBorderRight(CellStyle.BORDER_THIN);
		tStyle.setBorderTop(CellStyle.BORDER_THIN);
		cell.setCellStyle(tStyle);
		/*���õ�Ԫ��ʽ*/
	}
	
	public static void SetTotalABody(Row row_body,OriOrder order,XSSFWorkbook workbook){
		/*���ö�Ӧ�������*/
		Cell cell_body = null;
		cell_body = row_body.createCell(0);
		cell_body.setCellValue(order.getOrderNum());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(1);
		cell_body.setCellValue(order.getInput());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(2);
		cell_body.setCellValue(order.getDebt());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(3);
		cell_body.setCellValue(order.getTotal());
		SetCellStyle(workbook,cell_body);
		
		cell_body = row_body.createCell(4);
		cell_body.setCellValue(order.getClient());
		SetCellStyle(workbook,cell_body);
		/*���ö�Ӧ�������*/
	}
	
	/*���ڻ���ĵ��������������*/
	public  void SetTotalAFormBody(java.util.List<OriOrder> list_at,Sheet sheet1){
		
		for(int i = 0;i<list_at.size();i++){
			Row row_body = sheet1.createRow(i+1);
			OriOrder order = list_at.get(i);
			
			/*���ö�Ӧ�������*/
			Cell cell_body = null;
			cell_body = row_body.createCell(0);//���������豸���/���ҡ����ʺ�ͬ��
			cell_body.setCellValue(order.getNum());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("contract"));//���������豸���/���ҡ����ʺ�ͬ��
			cell_body.setCellValue(order.getOrderNum());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("inputMonth"));
			cell_body.setCellValue(order.getInput());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("debetMoney"));
			cell_body.setCellValue(order.getDebt());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("totalMoney"));
			cell_body.setCellValue(order.getTotal());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("clientName"));//���ÿͻ�����
			cell_body.setCellValue(order.getClient());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("cusCompanyid"));//���֤����/��֯��������֤
			cell_body.setCellValue(order.getId().getCuscompanyid());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("productTime"));//����ʱ��
			cell_body.setCellValue(order.getProductTime());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("paymentOwner"));//��������
			cell_body.setCellValue(order.getOwnerProduct());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("province"));//������Ա�绰
			cell_body.setCellValue(order.getProvince());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(NameToValue_Ori("paymentNature"));//������Ա�绰
			cell_body.setCellValue(order.getId().getPaymentNature());
			SetCellStyle(wXssfWorkbook,cell_body);
/*			cell_body = row_body.createCell(Ori_Excel_Format.cp_weixin);//������ϵ��΢��
			cell_body.setCellValue(order.getCustomweixin());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_name);//������ϵ������
			cell_body.setCellValue(order.getCustomname());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_phone);//������ϵ�˵绰
			cell_body.setCellValue(order.getCustomphone());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.cp_cardid);//������ϵ�����֤
			cell_body.setCellValue(order.getCuscompanyid());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_name);//������Ա����
			cell_body.setCellValue(order.getAsname());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_email);//������Ա����
			cell_body.setCellValue(order.getAsemail());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Ori_Excel_Format.as_phone);//������Ա�绰
			cell_body.setCellValue(order.getAsphone());
			SetCellStyle(wXssfWorkbook,cell_body);
			
*/
			
			/*���ö�Ӧ�������*/
		}
	}
	
	/*���ڸ�����Ϣ�ĵ��������������*/
	public void SetPayPecordFormBody(java.util.List<PayRecord> list_nbp,Sheet sheet1){
		for (int i = 0; i < list_nbp.size(); i++) {
			Row row_body = sheet1.createRow(i+1);
			
			PayRecord payRecord = list_nbp.get(i);
			
			Cell cell_body = null;
			cell_body = row_body.createCell(Export_PayRecord.payer);
			cell_body.setCellValue(payRecord.getPayer());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.paymoney);
			cell_body.setCellValue(payRecord.getPayMoney());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.payway);
			cell_body.setCellValue(payRecord.getPayWay());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.receiver);
			cell_body.setCellValue(payRecord.getReceiver());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_PayRecord.connectperson);
			cell_body.setCellValue(payRecord.getConnPerson());
			SetCellStyle(wXssfWorkbook,cell_body);
		}
	}
	
	public void SetBinputFormBody(java.util.List<BankInput> list_binput,Sheet sheet1){
		for (int i = 0; i < list_binput.size(); i++) {
			Row row_body = sheet1.createRow(i+1);
			
			BankInput bInput = list_binput.get(i);
			
			Cell cell_body = null;
			cell_body = row_body.createCell(Export_Binput.payer);
			cell_body.setCellValue(bInput.getPayer());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.paymoney);
			cell_body.setCellValue(bInput.getMoney());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.payway);
			cell_body.setCellValue(bInput.getPayWay());
			SetCellStyle(wXssfWorkbook,cell_body);
			
			cell_body = row_body.createCell(Export_Binput.payee);
			cell_body.setCellValue(bInput.getPayee());
			SetCellStyle(wXssfWorkbook,cell_body);
			
		}
	}
	
	public static void SetTotalAHead(XSSFWorkbook workbook,Sheet sheet1){
		Row row = sheet1.createRow(0);
		ArrayList <String> list = new ArrayList<String>();
		list.add("������");
		list.add("������");
		list.add("������");
		list.add("�����ܶ�");
		list.add("�ͻ���Ϣ");
		for(int i = 0;i<list.size();i++){
			Cell cell = row.createCell(i);
			cell.setCellValue(list.get(i));
			SetCellStyle(workbook,cell);
		}
	}
	
	public void SetFormHead(Sheet sheet,java.util.List<String> formhead){
		Row row = sheet.createRow(0);

		for(int i = 0;i<formhead.size();i++){
			Cell cell = row.createCell(i);
			cell.setCellValue(formhead.get(i));
			SetCellStyle(wXssfWorkbook,cell);
		}
	}
	
	public static String CreateLink(String dirname,String filename){
		String links = null;
		InetAddress iAddress = null;
		try {
			iAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ip = iAddress.getHostAddress();
		links = "http://" + ip + ":8080" + "/check_Accout/" + dirname + "/" + filename;
		return links;
	}
	
	public static java.util.List<OriOrder> ChooseAccout(String userType,String work_id,SessionFactory wSeFactory) {
		Total_Account_Dao tDao = new Total_Account_Dao(wSeFactory);
		java.util.List list_at = null;
		if (userType.equals("bm")) {
			list_at = tDao.GetTolAccount();
		}
		else if (userType.equals("bu")) {
			list_at = tDao.FindBySpeElement_S("owner", work_id);
		}
		
		return list_at;
	}
	
	public static void ReadExcel(String filename) {
		try {
			FileInputStream file = new FileInputStream(new File(filename));
			BufferedInputStream in = new BufferedInputStream(file);
		//	POIFSFileSystem fs = new POIFSFileSystem(in);
			
			XSSFWorkbook wb = new XSSFWorkbook(file);
			XSSFSheet st = wb.getSheetAt(0);
			
			Iterator<Row> rowIterator = st.iterator();
			while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                
                while (cellIterator.hasNext()) 
                {
                    Cell cell = cellIterator.next();
                    //Check the cell type and format accordingly
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                        	if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
                       
                        		Date date =	cell.getDateCellValue();
                        		String value = new SimpleDateFormat("yyyy-MM-dd").format(date);          
                        		System.out.print(value);
                        	}
                        	else
                        		System.out.print(cell.getNumericCellValue() + "t");
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "t");
                            break;
                        default:
                        	System.out.println("unknow format");
                        	break;
                    }
                }
                System.out.println("");
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Excel_Row> ReadExcel_Table(InputStream inputStream) {
		try {
			
			ArrayList<Excel_Row> excel_table = new ArrayList<>();
			
			XSSFWorkbook wb = new XSSFWorkbook(inputStream);
			XSSFSheet st = wb.getSheetAt(0);
			
			Iterator<Row> rowIterator = st.iterator();
			while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                
                String cell_svalue;//�����excel�ж�����һ����Ԫ��ֵ
                Excel_Row eRow = new Excel_Row(); //�����excel�ж�����һ�е�ֵ
                
                while (cellIterator.hasNext())//ѭ����ȡexcel�е�һ�� 
                {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                        	if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        		Date date =	cell.getDateCellValue();
                        		cell_svalue = new SimpleDateFormat("yyyy/MM/dd").format(date); 
                        		logger.info("time:" + cell.getDateCellValue());
                        	}
                        	else{
                        		logger.info("num:" + cell.getNumericCellValue());
                        		cell_svalue = Double.toString(cell.getNumericCellValue());
                        	}                        		
                            break;
                        case Cell.CELL_TYPE_STRING:
                        	cell_svalue = cell.getStringCellValue();
                            logger.info("string" + cell.getStringCellValue());
                            break;
                        default:
                        	cell_svalue = null;
                        	logger.error("unknow format");
                        	break;
                    }
                    
            //        if (cell_svalue != null) {
						eRow.list.add(cell_svalue);
			//		}  
                }
         //       table.add(eRow);
                excel_table.add(eRow);
            }

			return excel_table;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
	}

	//��excel���е�һ�н���ΪOriOrder�еĸ�����Ա
	public JSONObject Row_To_Ob_OriOrder(int row_id,ArrayList<Excel_Row> table,Agent agent){
		logger.info("Row_To_Ob at : " + (row_id+1) + "��");
		JSONObject jsonObject = new JSONObject();
		String errmsg = "";
		OriOrder order = new OriOrder();
		jsonObject.element("order", order);
		Excel_Row re_row = table.get(row_id);
		
		String num_s = re_row.list.get(0);
		if (num_s == null) { 
			errmsg = "���Ϊ����,����Ϊ��";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		
		String paymentNature = re_row.list.get(Ori_Excel_Format.paymentNature.compareTo(Ori_Excel_Format.num));
		String cuscompanyid = re_row.list.get(Ori_Excel_Format.cusCompanyid.compareTo(Ori_Excel_Format.num));
		String clientName = re_row.list.get(Ori_Excel_Format.clientName.compareTo(Ori_Excel_Format.num));
		if (clientName == null ) {
			errmsg = "���������ʽ����" + (row_id+1) + "���û���Ϊ��";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		if (paymentNature == null) {
			errmsg = "���������ʽ����" + (row_id+1) + "�л�������Ϊ��";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		if (cuscompanyid == null) {
			errmsg = "���������ʽ����" + (row_id+1) + "�пͻ���˾idΪ��";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		
		OriOrderId orderId = new OriOrderId();
		orderId.setCuscompanyid(cuscompanyid);
		orderId.setPaymentNature(paymentNature);
		//order.setPaymentNature(paymentNature);//���û�������
		order.setId(orderId);
		order.setClient(clientName);//���ÿͻ�����
		//order.setCuscompanyid(re_row.list.get(Ori_Excel_Format.cusCompanyid.compareTo(Ori_Excel_Format.num)));//���ÿͻ����֤������֯��������֤
		order.setOrderNum(re_row.list.get(Ori_Excel_Format.contract.compareTo(Ori_Excel_Format.num)));//���ú�ͬ��
		order.setProductTime(re_row.list.get(Ori_Excel_Format.productTime.compareTo(Ori_Excel_Format.num)));//����ʱ��
		order.setOwnerProduct(re_row.list.get(Ori_Excel_Format.paymentOwner.compareTo(Ori_Excel_Format.num)));//��������
		
		String totalMoney = re_row.list.get(Ori_Excel_Format.totalMoney.compareTo(Ori_Excel_Format.num));
		String debetMoney = re_row.list.get(Ori_Excel_Format.debetMoney.compareTo(Ori_Excel_Format.num));
		if (totalMoney == null || debetMoney == null) {
			errmsg = "���������ʽ����:��" + (row_id+1) + "�� �ܶ����������Ϊ����������";
			logger_error.error(errmsg);
			jsonObject.element("errmsg", errmsg);
			jsonObject.element("flag", -1);
			return jsonObject;
		}
		order.setNum(Double.valueOf(num_s).intValue());
		order.setTotal(Double.valueOf(totalMoney).doubleValue());//�����ܶ�
		order.setDebt(Double.valueOf(debetMoney).doubleValue());//����������
		order.setOwner(agent.getAgentId());//���û����¼������
		order.setAsname(agent.getAgentConnectpname());
		order.setAsphone(agent.getAgentCpphone());
		order.setAsemail(agent.getAgentCpemail());
		order.setProvince(re_row.list.get(Ori_Excel_Format.province.compareTo(Ori_Excel_Format.num)));
		order.setInput(0d);
		
		errmsg = "��������ɹ�";
		jsonObject.element("errmsg", errmsg);
		jsonObject.element("flag", 0);
		jsonObject.element("OriOrder", order);
		return jsonObject;
	}
	
	//����������excel�����ΪOriOrders
	public JSONObject Table_To_Ob_OriOrders(ArrayList<Excel_Row> excel_table,Agent agent) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		//ArrayList<OriOrder> orders = new ArrayList<OriOrder>();
		
		for (int i = 1; i < excel_table.size(); i++) {
			JSONObject re_js = Row_To_Ob_OriOrder(i,excel_table,agent);

			if (re_js.getInt("flag") == 0) {
				//OriOrder in_ori = (OriOrder) re_js.get("OriOrder");
				OriOrder in_ori = (OriOrder) JSONObject.toBean(re_js.getJSONObject("OriOrder"), OriOrder.class);
				//orders.add(in_ori);
				jsonArray.add(in_ori);
			}
			else {
				logger_error.error("��ȡ�����ĵ�" + (i+1) + "������ʧ��");
				jsonObject = re_js;
				return jsonObject;
			}
		}
		
		jsonObject.element("flag", 0);
		jsonObject.element("orders", jsonArray);
		return jsonObject;
	}
	
	//��excel���е�һ�н���ΪBankInput�еĸ�����Ա
	public BankInput Row_To_Ob_BankIn(int row_id,ArrayList<Excel_Row> table,String ownerid) {
		logger.info("Row_To_Ob at:" + (row_id+1) + "��");
		
		BankInput bInput = new BankInput();
		Excel_Row eRow = table.get(row_id);
		
		String num_s = eRow.list.get(0);
		bInput.setNum(Double.valueOf(num_s).intValue());//�������
		bInput.setPayee(eRow.list.get(NameToValue_BankInput("inAccountName")));//�����տ�������
		bInput.setPayWay(eRow.list.get(NameToValue_BankInput("inWay")));//�����տʽ
		bInput.setInputTime(eRow.list.get(NameToValue_BankInput("inTime")));//���õ�������
		bInput.setActualPayer(eRow.list.get(NameToValue_BankInput("actualPayer")));//����ʵ�ʸ�����
		
		logger.info(NameToValue_BankInput("inMoney"));
		Double inMoney = Double.valueOf(eRow.list.get(NameToValue_BankInput("inMoney"))).doubleValue();
		bInput.setMoney(inMoney);//�����տ���
		
		bInput.setPayer(eRow.list.get(NameToValue_BankInput("client")));//���ø���������
		bInput.setCuscompanyid(eRow.list.get(NameToValue_BankInput("cuscompanyid")));//�ͻ���
		bInput.setPayerAccount(eRow.list.get(NameToValue_BankInput("outAccount")));//���ø������ʺ�
		bInput.setPaymentNature(eRow.list.get(NameToValue_BankInput("paymentNature")));
		bInput.setStatus(false);
		bInput.setIsConnect(false);
		
		bInput.setConnectClient("");
		bInput.setConnectNum(0);
		bInput.setOwner(ownerid);
		return bInput;
	}

	//����������excel�����ΪBankInputs
	public ArrayList<BankInput> Table_To_Ob_BankIn(ArrayList<Excel_Row> excel_table,String owner_id) {
		ArrayList<BankInput> bankInputs = new ArrayList<BankInput>();
		
		for (int i = 1; i < excel_table.size(); i++) {
			bankInputs.add(Row_To_Ob_BankIn(i, excel_table,owner_id));
		}
		
		return bankInputs;
	}

	public void Test_Table(ArrayList<Excel_Row> excel_table){
		for (int i = 0; i < excel_table.size(); i++) {
			Excel_Row excel_Row = excel_table.get(i);
			excel_Row.Test_list();
		}
	}
	
	public class Excel_Row{
		public ArrayList<String> list;
		public Excel_Row() {
			list = new ArrayList<String>();
	}
		
	public void Test_list(){
		for(int i = 0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}
		// TODO Auto-generated constructor stub
	}
	
	//����excel��ĸ�ʽ
/*	public class Ori_Excel_Format{
		public static final int province = 0;//ʡ��
		public static final int client_name = 1;//�ͻ�����
		public static final int client_id = 2;//�ͻ����֤����֯��������֤
		public static final int contract = 3;//�豸���۱��/��ͬ��
		public static final int product_time = 4;//����ʱ��
		public static final int owner_product = 5;//��������
		public static final int total_money = 6;//��ͬ�ܶ�
		public static final int debet_money = 7;//���������
		public static final int as_name = 8;//�����̹���Ա
		public static final int as_phone = 9;//�����̵绰
		public static final int as_email = 10;//����������
		public static final int cp_name = 11;//������ϵ��
		public static final int cp_cardid = 12;//������ϵ�����֤
		public static final int cp_phone = 13;//������ϵ���ƶ��绰
		public static final int cp_weixin = 14;//������ϵ��΢��
		public static final int input = 15;//���»ؿ�
	}*/
	
	public enum Ori_Excel_Format{
		num,//���
		province,//ʡ��
		clientName,//�ͻ�����
		cusCompanyid,//�ͻ�id
		dingdanNum,//������
		productName,//��Ʒ����
		facilityName,//�豸����
		contract,//��ͬ��
		productTime,//����ʱ��
		paymentOwner,//��������
		paymentNature,//��������
		inputNature,//�ؿ�����
		totalMoney,//��ͬ�ܶ�
		debetMoney,//������
		actualPayer,//ʵ�ʸ�����
		payway,//���ʽ
		inputMonth,//���»ؿ�
		inputClient//�ͻ��ؿ�
	}
	
	public static int NameToValue_Ori(String name){
		return Ori_Excel_Format.valueOf(name).compareTo(Ori_Excel_Format.num);
	}
	
	//�տ�excel��ĸ�ʽ
/*	public class BankInput_Excel_Format{
		public static final int in_account_name = 1;//�տ��ʺ�����
		public static final int in_account_num = 2;//�տ��ʺ�
		public static final int in_way = 3;//�տʽ
		public static final int in_linkcel_num = 4;//�տ�׺�
		public static final int in_Time = 5;//��������
		public static final int in_money= 6;//���ʽ��
		public static final int out_account_name = 7;//�����ʺ�����
		public static final int out_account_num = 8;//�����ʺ�
	}*/
	
	public enum BankInput_Excel_Format{
		num,//���
		province,//ʡ��
		inAccountName,//�տ��˻�
		outAccount,//�����˻�
		actualPayer,//ʵ�ʸ�����
		inTime,//��������
		inWay,//���ʽ
		piaohao,//Ʊ��
		inMoney,//�տ���
		client,//�����ͻ�����
		cuscompanyid,//�ͻ���
		paymentNature,//��������
		partMoney,//��ֽ��
		remark//�տ�ժҪ
	}
	
	public static int NameToValue_BankInput(String name){
		return BankInput_Excel_Format.valueOf(name).compareTo(BankInput_Excel_Format.num);
	}
	
	public static class My_Date{
		public My_Date() {
	}
		// TODO Auto-generated constructor stub	
		public  static Date StringToDate(String date_s){
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sFormat.parse(date_s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static String DateToString(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date_s = sdf.format(date);
		return date_s;
	}
	}
}
