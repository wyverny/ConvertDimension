import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.*;
import org.w3c.dom.*;

public class ConvertDimen {

	static final String sw320dp = "sw320dp";
	static final String sw360dp = "sw360dp";
	static final String sw400dp = "sw400dp";
	static final String sw600dp = "sw600dp";
	static final String sw720dp = "sw720dp";
	static final String sw800dp = "sw800dp";
	static String expression = null, originFilePath = "", savingPath = null;
	static int inputType;
	static NodeList nodeList;
	static String[] mDimenName;
	static String[] types = new String[] {sw320dp, sw360dp, sw400dp, sw600dp, sw720dp, sw800dp};
	static File xmlFile = null;
	// C:/work_tikkle/res/values-sw360dp/dimens.xml
	// /Users/smilee_yang/Documents/workspace/SupportDifferentSize/res/values/dimens.xml
	
	public static void main(String[] args) {

		try {
			System.out.print("Please enter the original dimens.xml file path: ");
			do {
				originFilePath = readKeyIn();
				xmlFile = new File(originFilePath);
				if (xmlFile.exists()) {
					int tail = originFilePath.indexOf("/res/"); 
					savingPath = originFilePath.substring(0, tail + 4); // /Users/smilee_yang/Documents/workspace/SupportDifferentSize/res
					//System.out.println("Correct file, file name: " + xmlFile.getName());
					
					DocumentBuilderFactory docBuFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docBuFactory.newDocumentBuilder();
					Document xmlDoc = docBuilder.parse(xmlFile);
	
					XPath xPath = XPathFactory.newInstance().newXPath();
	
					System.out.println("-------------------------");
	
					// Get dimension name
					expression = "/resources/dimen/attribute::name"; // get abc from <dimen name="abc">123dp</dimen>
					nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
	
					mDimenName = new String[nodeList.getLength()];
					String[] nowDimenValue = new String[nodeList.getLength()];
	
					for (int i = 0; i < nodeList.getLength(); i++) {
						mDimenName[i] = nodeList.item(i).getFirstChild().getNodeValue();
					}
	
					// Get dimension value
					expression = "/resources/dimen[@name]"; // get 123dp from <dimen name="abc">123dp</dimen>
					nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
	
					for (int i = 0; i < nodeList.getLength(); i++) {
						nowDimenValue[i] = nodeList.item(i).getFirstChild().getNodeValue();
						System.out.println("DimenName: "+mDimenName[i]+" & DimenValue="+nowDimenValue[i]);
					}
					if (nowDimenValue.length != 0) {
						System.out.println("-------------------------");
						System.out.print("This file is based on - 1.sw320dp 2.sw360dp 3.sw400dp 4.sw600dp 5.sw720dp 6.sw800dp (plz enter the number): ");
						String densityType = null;
						int nowChoice = 0;
						do {
							densityType = readKeyIn();
							if (densityType.matches("[1-6]")) {
								nowChoice = Integer.valueOf(densityType);
								System.out.println("Start convert process, convert from " + types[nowChoice-1] + " to the others.");
								inputType = nowChoice;
								filterUnit(nowDimenValue);
							} else {
								System.out.print("Please enter the number either 1 or 2 or 3 or 4 or 5 or 6 (1.sw320dp 2.sw360dp 3.sw400dp 4.sw600dp 5.sw720dp 6.sw800dp): ");
							}
						} while (!densityType.matches("[1-6]"));
						System.out.println("Convert process completed.");
					} else {
						System.out.println("Can't get dimension values, please check your dimens.xml, thanks.");
					}
				} else {
						System.out.print("The path is wrong. Please try again: ");
				}
			} while (!xmlFile.exists());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public static String readKeyIn() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String keyIn = null; 
		try {
			keyIn = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keyIn;
	}
	
	public static void filterUnit(String[] nowDimenValue) {
		String[] onlyValue = new String[nowDimenValue.length];
		for (int index = 0; index < nowDimenValue.length; index++) {
			// filter
			String filterUnit = "";
			Pattern pattern = Pattern.compile("[0-9]*(\\.)*[0-9]+");
			Matcher matcher = pattern.matcher(nowDimenValue[index]);
			while (matcher.find()) {
				filterUnit = filterUnit + matcher.group();
			}
			if (!filterUnit.equalsIgnoreCase("")) {
				onlyValue[index] = filterUnit;
//				System.out.println("onlyValue["+index+"]="+onlyValue[index]);
			}
		}
		writingLoop(nowDimenValue, onlyValue);
	}
	
	public static void writingLoop(String[] nowDimenValue, String[] onlyValue) {
		String[][] newDimenValue = new String[onlyValue.length][onlyValue.length];
		int formula = 5;
		Double[] formulaForSw320dp = new Double[] { 1.125, 1.25, 1.875, 2.25, 2.5 }; // sw320dp to sw360dp, sw400dp, sw600dp, sw720dp, sw800dp
		Double[] formulaForSw360dp = new Double[] { 0.89, 1.11, 1.67, 2.0, 2.22 }; // sw360dp to sw320dp, sw400dp, sw600dp, sw720dp, sw800dp
		Double[] formulaForSw400dp = new Double[] { 0.8, 0.9, 1.5, 1.8, 2.0 }; // sw400dp to sw320dp, sw360dp, sw600dp, sw720dp, sw800dp
		Double[] formulaForSw600dp = new Double[] { 0.53, 0.6, 0.67, 1.2, 1.33 }; // sw600dp to sw320dp, sw360dp, sw400dp, sw720dp, sw800dp
		Double[] formulaForSw720dp = new Double[] { 0.44, 0.5, 0.56, 0.83, 1.11 }; // sw720dp to sw320dp, sw360dp, sw400dp, sw600dp, sw800dp
		Double[] formulaForSw800dp = new Double[] { 0.4, 0.45, 0.5, 0.75, 0.9 }; // sw800dp to sw320dp, sw360dp, sw400dp, sw600dp, sw720dp
		BigDecimal bd = null;
		for (int times = 0; times < formula; times++) {
			for (int index = 0; index < onlyValue.length; index++) {
				Double[] formulas = null;
				switch (inputType) {
				case 1:
					formulas = formulaForSw320dp;
					break;
				case 2:
					formulas = formulaForSw360dp;
					break;
				case 3:
					formulas = formulaForSw400dp;
					break;
				case 4:
					formulas = formulaForSw600dp;
					break;
				case 5:
					formulas = formulaForSw720dp;
					break;
				case 6:
					formulas = formulaForSw800dp;
					break;
				}
				bd = new BigDecimal(Float.valueOf(onlyValue[index]) * formulas[times]);
				bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				newDimenValue[times][index] = nowDimenValue[index].replace(onlyValue[index], String.valueOf(bd));
				
//				System.out.println("newDimenValue["+times+"]["+index+"]="+newDimenValue[times][index]);
			}
		}
		bufferedFileWriter(newDimenValue);
	}

	public static void bufferedFileWriter(String[][] newDimenValue) {
		String confirmChoice = null;
		System.out.print("Convert success! Now saving path is: " + savingPath + "\nDo you want to save these files with this path? (Y/N) ");
		do {
			confirmChoice = readKeyIn();
			if (confirmChoice.equalsIgnoreCase("Y")) {
				System.out.println("Confirmed your saving path is: " + savingPath + ".");
			} else if (confirmChoice.equalsIgnoreCase("N")) {
				File checkPath = null;
				System.out.print("Please enter a path that you want to save these files: ");
				do {
					savingPath = readKeyIn();
					checkPath = new File(savingPath);
					if (checkPath.exists()) {
						System.out.print("Correct path. ");
					} else if (!checkPath.exists()) {
						System.out.print("Sorry, this path is not a correct path, please try again: ");
					}
				}while (!checkPath.exists());
			} else {
				System.out.print("Please enter Y or N: ");
			}
		} while (!confirmChoice.equalsIgnoreCase("Y") && !confirmChoice.equalsIgnoreCase("N"));
		int skipType = inputType-1;
		int nowSavingType = 0;
		int arrayIndex = 0;
		String fileType = null;
		do {
			fileType = types[nowSavingType];
			if (nowSavingType == skipType) {
				nowSavingType++;
				continue;
			}
			String mFilePath = savingPath+"/values-"+fileType+"/"; 
			File dir = new File(mFilePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			Writer fw = null;
			BufferedWriter bw = null;
			String theDimen = null;
			try {
				fw = new FileWriter(mFilePath + "dimens.xml", false);
				bw = new BufferedWriter(fw);
				bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
				bw.newLine();
				bw.write("<resources>");
				bw.newLine();
				bw.newLine();
				for (int i = 0; i < mDimenName.length; i++) {
					theDimen = "    <dimen name=\"" + mDimenName[i] + "\">" + newDimenValue[arrayIndex][i] + "</dimen>";
					bw.write(theDimen);
					bw.newLine();
				}
				bw.newLine();
				bw.write("</resources>");
				bw.close();
			} catch (IOException e) {
				System.err.println("Error writing the file : ");
				e.printStackTrace();
			} finally {
				if (bw != null && fw != null) {
					try {
						bw.close();
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			nowSavingType++;
			arrayIndex++;
		} while (nowSavingType < types.length);
	}
}
