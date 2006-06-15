/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.vqwiki.servlets;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.vqwiki.Change;
import org.vqwiki.TopicVersion;
import org.vqwiki.VersionManager;
import org.vqwiki.WikiBase;
import org.vqwiki.servlets.beans.StatisticsAuthorBean;
import org.vqwiki.servlets.beans.StatisticsMonthBean;
import org.vqwiki.servlets.beans.StatisticsOneWikiBean;
import org.vqwiki.servlets.beans.StatisticsVWikiBean;
import org.vqwiki.utils.Utilities;

/**
 * This servlet provides some general statisitcs for the usage of wiki
 *
 * This class was created on 09:34:30 19.07.2003
 *
 * @author $Author: wrh2 $
 */
public class StatisticServlet extends LongLastingOperationServlet {

	/** Logging */
	private static final Logger logger = Logger.getLogger(StatisticServlet.class);
	private StatisticsVWikiBean vwikis;

	/**
	 * Constructor
	 *
	 *
	 */
	public StatisticServlet() {
		super();
	}

	/**
	 * Handle post request.
	 * Generate a RSS feed and send it back as XML.
	 *
	 * @param request  The current http request
	 * @param response What the servlet will send back as response
	 *
	 * @throws ServletException If something goes wrong during servlet execution
	 * @throws IOException If the output stream cannot be accessed
	 *
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		super.doPost(request, response);
	}

	/**
	 *
	 */
	public void run() {
		vwikis = new StatisticsVWikiBean();
		NumberFormat nf = NumberFormat.getInstance(locale);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(1);
		Collection allWikis;
		try {
			allWikis = WikiBase.getInstance().getVirtualWikiList();
		} catch (Exception e) {
			allWikis = Collections.EMPTY_LIST;
		}
		if (!allWikis.contains(WikiBase.DEFAULT_VWIKI)) {
			allWikis.add(WikiBase.DEFAULT_VWIKI);
		}
		HashMap revisionsOverMonths = new HashMap();
		HashMap topicsOverMonths = new HashMap();
		HashMap authors = new HashMap();
		int revisionsLastWeek = 0;
		int topicsLastWeek = 0;
		Calendar cal = new GregorianCalendar();
		Calendar twelveMonthsAgo = new GregorianCalendar();
		twelveMonthsAgo.setTime(new java.util.Date());
		twelveMonthsAgo.add(Calendar.MONTH, -12);
		Calendar lastWeekAgo = new GregorianCalendar();
		lastWeekAgo.setTime(new java.util.Date());
		lastWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
		int allWikiCount = 0;
		int pageCount = 0;
		for (Iterator iterator = allWikis.iterator(); iterator.hasNext(); allWikiCount++) {
			setProgress(allWikiCount, allWikis.size(), 0, 0);
			String currentWiki = (String) iterator.next();
			StatisticsOneWikiBean onewiki = new StatisticsOneWikiBean();
			onewiki.setName(currentWiki);
			int numberOfChanges = 0;
			int numberOfTopics = 0;
			try {
				pageCount = 0;
				Collection topics = WikiBase.getInstance().getSearchEngineInstance().getAllTopicNames(currentWiki);
				numberOfTopics = topics.size();
				onewiki.setNumpages(String.valueOf(numberOfTopics));
				for (Iterator iter = topics.iterator(); iter.hasNext(); pageCount++) {
					setProgress(allWikiCount, allWikis.size(), pageCount, numberOfTopics);
					try {
						String topic = (String) iter.next();
						VersionManager versionManager = WikiBase.getInstance().getVersionManagerInstance();
						List versions = versionManager.getAllVersions(currentWiki, topic);
						int numberOfVersions = versions.size();
						//numberOfVersions = (numberOfVersions < 1)?1:numberOfVersions;
						numberOfChanges += numberOfVersions;
						HashMap hasMonthModification = new HashMap();
						boolean hasWeekModification = false;
						for (Iterator versionIterator = versions.iterator(); versionIterator.hasNext();) {
							TopicVersion aVersion = (TopicVersion) versionIterator.next();
							cal.setTime(new java.util.Date(aVersion.getRevisionDate().getTime()));
							// is revision older than 12 months?
							if (cal.after(twelveMonthsAgo)) {
								Integer month = new Integer(cal.get(Calendar.MONTH));
								int count = 0;
								if (null != revisionsOverMonths.get(month)) {
									count = ((Integer) revisionsOverMonths.get(month)).intValue();
								}
								count++;
								revisionsOverMonths.put(month, new Integer(count));
								if (null == hasMonthModification.get(month)) {
									count = 0;
									if (null != topicsOverMonths.get(month)) {
										count = ((Integer) topicsOverMonths.get(month)).intValue();
									}
									count++;
									topicsOverMonths.put(month, new Integer(count));
									hasMonthModification.put(month, new Boolean(true));
								}
							} // otherwise forget about this entry
							if (cal.after(lastWeekAgo)) {
								revisionsLastWeek++;
								if (!hasWeekModification) {
									topicsLastWeek++;
									hasWeekModification = true;
								}
							}
							// determine author
							Collection changesOnThisDay = WikiBase.getInstance().getChangeLogInstance().getChanges(currentWiki, new java.util.Date(aVersion.getRevisionDate().getTime()));
							if (changesOnThisDay != null) {
								for (Iterator changeIterator = changesOnThisDay.iterator(); changeIterator.hasNext();) {
									Change aChange = (Change) changeIterator.next();
									if (aChange.getTopic().equals(aVersion.getTopicName())) {
										if (aChange.getUser() != null) {
											int count = 0;
											if (null != authors.get(aChange.getUser())) {
												// FIXME - more than four levels of indentation
												// is usually a sign that a function needs to
												// be broken up or simplified...
												count = ((Integer) authors.get(aChange.getUser())).intValue();
											}
											count++;
											authors.put(aChange.getUser(), new Integer(count));
										}
									}
								}
							}
						}
					} catch (Exception e) {
						logger.fatal("Exception", e);
					}
				}
			} catch (Exception ex) {
				logger.error(ex);
			}
			// fill months
			cal.setTime(new java.util.Date());
			List monthList = new ArrayList();
			for (int i = 0; i < 12; i++) {
				Integer month = new Integer((cal.get(Calendar.MONTH) - i + 12) % 12);
				Integer changes = (Integer) revisionsOverMonths.get(month);
				if (changes == null) changes = new Integer(0);
				Integer pages = (Integer) topicsOverMonths.get(month);
				if (pages == null) pages = new Integer(0);
				StatisticsMonthBean mb = new StatisticsMonthBean();
				if (i == 0) {
					mb.setName(Utilities.resource("month.this", locale));
				} else if (i == 1) {
					mb.setName(Utilities.resource("month.last", locale));
				} else {
					mb.setName(Utilities.resource("month.name" + month.toString(), locale));
				}
				mb.setChanges(changes.toString());
				mb.setPages(pages.toString());
				mb.setRatio(nf.format(changes.doubleValue() / pages.doubleValue()));
				monthList.add(mb);
			}
			onewiki.setMonths(monthList);
			onewiki.setNumchangeslw(String.valueOf(revisionsLastWeek));
			onewiki.setNumpageslw(String.valueOf(topicsLastWeek));
			onewiki.setRatiolw(nf.format(((double) revisionsLastWeek / (double) topicsLastWeek)));
			onewiki.setNumchanges(String.valueOf(numberOfChanges));
			onewiki.setNummodifications(nf.format(((double) numberOfChanges / (double) numberOfTopics)));
			// fill up authors
			Set authorNames = authors.keySet();
			List authorList = new ArrayList();
			for (Iterator authorIterator = authorNames.iterator(); authorIterator.hasNext();) {
				String anAuthorName = (String) authorIterator.next();
				StatisticsAuthorBean ab = new StatisticsAuthorBean();
				ab.setName(anAuthorName);
				ab.setChanges(((Integer) authors.get(anAuthorName)).toString());
				authorList.add(ab);
			}
			onewiki.setAuthors(authorList);
			vwikis.getVwiki().add(onewiki);
		}
		progress = PROGRESS_DONE;
	}

	/**
	 * Set the progress
	 * @param allWikiCount Current wiki, we are processing
	 * @param allWikiSize  Number of wikis (overall)
	 * @param pageCount Current page we are processing
	 * @param pageSize Number of pages of this wiki
	 */
	private void setProgress(int allWikiCount, int allWikiSize, int pageCount, int pageSize) {
		double one = 100.0 / (double) allWikiSize;
		if (pageSize == 0) {
			progress = Math.min((int) ((double) allWikiCount * one), 99);
		} else {
			progress = Math.min((int) ((double) allWikiCount * one + (double) pageCount * one / (double) pageSize), 99);
		}
	}

	/**
	 * We are done. Go to result page.
	 * @see vqwiki.servlets.LongLastingOperationServlet#dispatchDone(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void dispatchDone(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("virtualwikis", vwikis);
		request.setAttribute("title", Utilities.resource("statistics.title", request.getLocale()));
		dispatch("/WEB-INF/jsp/statistics.jsp", request, response);
	}

	/**
	 * Handle get request.
	 * The request is handled the same way as the post request.
	 *
	 * @see doPost()
	 *
	 * @param httpServletRequest  The current http request
	 * @param httpServletResponse What the servlet will send back as response
	 *
	 * @throws ServletException If something goes wrong during servlet execution
	 * @throws IOException If the output stream cannot be accessed
	 *
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException {
		this.doPost(httpServletRequest, httpServletResponse);
	}
}

/*
 * Log:
 *
 * $Log$
 * Revision 1.7  2006/04/23 06:36:56  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.6  2003/10/05 05:07:32  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.5  2003/07/23 13:45:19  mrgadget4711
 * ADD: progress information
 *
 * Revision 1.4  2003/07/23 00:34:26  mrgadget4711
 * ADD: Long lasting operations
 *
 * Revision 1.3  2003/07/21 09:19:39  mrgadget4711
 * Fixes
 *
 * Revision 1.2  2003/07/20 20:56:19  garethc
 * 2.5.2
 *
 * Revision 1.1  2003/07/19 13:22:59  mrgadget4711
 * ADD: Statistic capabilities
 *
 * ------------END------------
 */