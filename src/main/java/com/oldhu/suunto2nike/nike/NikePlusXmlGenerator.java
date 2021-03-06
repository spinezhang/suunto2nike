package com.oldhu.suunto2nike.nike;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class NikePlusXmlGenerator
{
	private static final String DATE_TIME_FORMAT_NIKE = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String DATE_TIME_FORMAT_SUUNTO = "yyyy-MM-dd HH:mm:ss";

	private SuuntoMove move;

	public NikePlusXmlGenerator(SuuntoMove move)
	{
		this.move = move;
	}

	private Document newDocument() throws Exception
	{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}

	public Document getXML() throws Exception
	{
		Document doc = newDocument();
		Element rootSportsData = Util.appendElement(doc, "sportsData");

		appendRunSummary(rootSportsData);
		appendTemplate(rootSportsData);
		appendGoalType(rootSportsData);
		appendUserInfo(rootSportsData);
		appendStartTimeElement(rootSportsData);
		appendExtendedDataList(rootSportsData);

		return doc;
	}

	private void appendRunSummary(Element parent) throws ParseException
	{
		Element runSummary = Util.appendElement(parent, "runSummary");
		appendRunSummaryStartTime(runSummary);
		appendRunSummaryTotals(runSummary);
		Util.appendElement(runSummary, "battery");
	}

	private String getNikeFormatStartTime() throws ParseException
	{
		SimpleDateFormat dfNike = new SimpleDateFormat(DATE_TIME_FORMAT_NIKE);
		SimpleDateFormat dfSuunto = new SimpleDateFormat(DATE_TIME_FORMAT_SUUNTO);
		Date startTime = dfSuunto.parse(move.getStartTime());
		String startTimeNikeStr = dfNike.format(startTime);
		return String.format(Locale.US, "%s:%s", startTimeNikeStr.substring(0, 22), startTimeNikeStr.substring(22));
	}

	private void appendRunSummaryStartTime(Element runSummary) throws ParseException
	{
		Util.appendElement(runSummary, "time", getNikeFormatStartTime());
	}

	private void appendRunSummaryTotals(Element runSummary)
	{
		Util.appendElement(runSummary, "duration", move.getDuration());
		float distance = move.getDistance() / 1000.0f;
		Util.appendElement(runSummary, "distance", String.format(Locale.US, "%.4f", distance), "unit", "km");
		Util.appendElement(runSummary, "calories", move.getCalories());
	}

	private void appendTemplate(Element sportsDataElement)
	{
		Element templateElement = Util.appendElement(sportsDataElement, "template");
		Util.appendCDATASection(templateElement, "templateName", "Basic");
	}

	private void appendGoalType(Element sportsDataElement)
	{
		Util.appendElement(sportsDataElement, "goal", null, "type", "", "value", "", "unit", "");
	}

	private void appendUserInfo(Element sportsDataElement)
	{
		Element userInfo = Util.appendElement(sportsDataElement, "userInfo");
		Util.appendElement(userInfo, "empedID", "XXXXXXXXXXX");
		Util.appendElement(userInfo, "weight");

		Util.appendElement(userInfo, "device", "iPod"); // iPod
		Util.appendElement(userInfo, "calibration");
	}

	private void appendStartTimeElement(Element sportsDataElement) throws Exception
	{
		Util.appendElement(sportsDataElement, "startTime", getNikeFormatStartTime());
	}

	private void appendExtendedDataList(Element sportsDataElement)
	{
		StringBuilder sbDistance = new StringBuilder();
		StringBuilder sbSpeed = new StringBuilder();
		StringBuilder sbHeartRate = new StringBuilder();

		for (int i = 0; i < move.getDistanceSamples().size(); ++i) {
			float distance = move.getDistanceSamples().get(i).floatValue() / 1000;
			sbDistance.append(String.format(Locale.US, "%.4f", distance));
			sbSpeed.append("0.0000");
			sbHeartRate.append(move.getHeartRateSamples().get(i));
			if (i < move.getDistanceSamples().size() - 1) {
				sbDistance.append(", ");
				sbSpeed.append(", ");
				sbHeartRate.append(", ");
			}
		}

		Element extendedDataListElement = Util.appendElement(sportsDataElement, "extendedDataList");
		Util.appendElement(extendedDataListElement, "extendedData", sbDistance, "dataType", "distance", "intervalType",
				"time", "intervalUnit", "s", "intervalValue", "10");
//		appendElement(extendedDataListElement, "extendedData", sbSpeed, "dataType", "speed", "intervalType", "time",
//				"intervalUnit", "s", "intervalValue", "10");
		Util.appendElement(extendedDataListElement, "extendedData", sbHeartRate, "dataType", "heartRate", "intervalType",
				"time", "intervalUnit", "s", "intervalValue", "10");
	}

}
