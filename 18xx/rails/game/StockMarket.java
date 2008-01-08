/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/StockMarket.java,v 1.9 2008/01/08 20:23:55 evos Exp $ */
package rails.game;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import rails.game.move.PriceTokenMove;
import rails.util.*;

public class StockMarket implements StockMarketI, ConfigurableComponentI
{

	protected HashMap<String, StockSpaceTypeI> stockSpaceTypes 
		= new HashMap<String, StockSpaceTypeI>();
	protected HashMap<String, StockSpaceI> stockChartSpaces 
		= new HashMap<String, StockSpaceI>();
	protected StockSpace stockChart[][];
	protected StockSpace currentSquare;
	protected int numRows = 0;
	protected int numCols = 0;
	protected ArrayList<StockSpaceI> startSpaces 
		= new ArrayList<StockSpaceI>();
	protected int[] startPrices;

	protected static StockMarketI instance;

	/* Game-specific flags */
	protected boolean upOrDownRight = false; /*
												 * Sold out and at top: go down
												 * right (1870)
												 */

	/* States */
	protected boolean gameOver = false; /*
										 * Some games have "rails.game over"
										 * stockmarket squares
										 */

	ArrayList<PublicCertificate> ipoPile;

	//ArrayList companiesStarted;

	public StockMarket()
	{
		instance = this;
	}

	public static StockMarketI getInstance()
	{
		return instance;
	}

	/**
	 * @see rails.game.ConfigurableComponentI#configureFromXML(org.w3c.dom.Element)
	 */
	public void configureFromXML(Tag tag) throws ConfigurationException
	{
		/* Read and configure the stock market space types */
		List<Tag> typeTags = tag.getChildren(StockSpaceTypeI.ELEMENT_ID);

		if (typeTags != null) {
			for (Tag typeTag : typeTags)
			{
				/* Extract the attributes of the Stock space type */
				String name = typeTag.getAttributeAsString(StockSpaceTypeI.NAME_TAG);
				if (name == null)
				{
					throw new ConfigurationException(LocalText.getText("UnnamedStockSpaceType"));
				}
				String colour = typeTag.getAttributeAsString(StockSpaceTypeI.COLOUR_TAG);
	
				/* Check for duplicates */
				if (stockSpaceTypes.get(name) != null)
				{
					throw new ConfigurationException(LocalText.getText("StockSpaceTypeConfiguredTwice", name));
				}
	
				/* Create the type */
				StockSpaceTypeI type = new StockSpaceType(name, colour);
				stockSpaceTypes.put(name, type);
	
				// Check the stock space type flags
				type.setNoBuyLimit(typeTag.getChild(StockSpaceTypeI.NO_BUY_LIMIT_TAG) != null);
				type.setNoCertLimit(typeTag.getChild(StockSpaceTypeI.NO_CERT_LIMIT_TAG) != null);
				type.setNoHoldLimit(typeTag.getChild(StockSpaceTypeI.NO_HOLD_LIMIT_TAG) != null);
			}
		}

		/* Read and configure the stock market spaces */
		List<Tag> spaceTags = tag.getChildren(StockSpaceI.ELEMENT_ID);
		StockSpaceTypeI type;
		int row, col;
		for (Tag spaceTag : spaceTags)
		{
			type = null;

			// Extract the attributes of the Stock space
			String name = spaceTag.getAttributeAsString(StockSpaceI.NAME_TAG);
			if (name == null)
			{
				throw new ConfigurationException(LocalText.getText("UnnamedStockSpace"));
			}
			String price = spaceTag.getAttributeAsString(StockSpaceI.PRICE_TAG);
			if (price == null)
			{
				throw new ConfigurationException(
				        LocalText.getText("StockSpaceHasNoPrice", name));
			}
			String typeName = spaceTag.getAttributeAsString(StockSpaceI.TYPE_TAG);
			if (typeName != null
					&& (type = (StockSpaceTypeI) stockSpaceTypes.get(typeName)) == null)
			{
				throw new ConfigurationException(
				        LocalText.getText("StockSpaceTypeUndefined", type));
			}

			if (stockChartSpaces.get(name) != null)
			{
				throw new ConfigurationException(
				        LocalText.getText("StockSpaceIsConfiguredTwice",  name));
			}

			StockSpaceI space = new StockSpace(name,
					Integer.parseInt(price),
					type);
			stockChartSpaces.put(name, space);

			row = Integer.parseInt(name.substring(1));
			col = (int) (name.toUpperCase().charAt(0) - '@');
			if (row > numRows)
				numRows = row;
			if (col > numCols)
				numCols = col;

			// Loop through the stock space flags
			if (spaceTag.getChild(StockSpaceI.START_SPACE_TAG) != null) {
					space.setStart(true);
					startSpaces.add(space);
			}
			space.setClosesCompany(spaceTag.getChild(StockSpaceI.CLOSES_COMPANY_TAG) != null);
			space.setEndsGame(spaceTag.getChild(StockSpaceI.GAME_OVER_TAG) != null);
			space.setBelowLedge(spaceTag.getChild(StockSpaceI.BELOW_LEDGE_TAG) != null);
			space.setLeftOfLedge(spaceTag.getChild(StockSpaceI.LEFT_OF_LEDGE_TAG) != null);

		}

		startPrices = new int[startSpaces.size()];
		for (int i = 0; i < startPrices.length; i++)
		{
			startPrices[i] = ((StockSpaceI) startSpaces.get(i)).getPrice();
		}

		stockChart = new StockSpace[numRows][numCols];
		Iterator it = stockChartSpaces.values().iterator();
		StockSpace space;
		while (it.hasNext())
		{
			space = (StockSpace) it.next();
			stockChart[space.getRow()][space.getColumn()] = space;
		}

		upOrDownRight = tag.getChild("UpOrDownRight") != null;

	}

	/**
	 * Final initialisations, to be called after all XML processing is complete.
	 * The purpose is to register fixed company start prices.
	 */
	public void init()
	{

		Iterator it = Game.getCompanyManager()
				.getAllPublicCompanies()
				.iterator();
		PublicCompanyI comp;
		//StockSpaceI space;
		while (it.hasNext())
		{
			comp = (PublicCompanyI) it.next();
			if (!comp.hasStarted() && comp.getParPrice() != null)
			{
				comp.getParPrice().addFixedStartPrice(comp);
			}
		}

	}

	/**
	 * @return
	 */
	public StockSpace[][] getStockChart()
	{
		return stockChart;
	}

	public StockSpace getStockSpace(int row, int col)
	{
		if (row >= 0 && row < numRows && col >= 0 && col < numCols)
		{
			return stockChart[row][col];
		}
		else
		{
			return null;
		}
	}

	public StockSpace getStockSpace(String name)
	{
		return (StockSpace) stockChartSpaces.get(name);
	}

	/*--- Actions ---*/
	
	public void start (PublicCompanyI company, StockSpaceI price) {
	    prepareMove (company, null, price);
	}

	public void payOut(PublicCompanyI company)
	{
		moveRightOrUp(company);
	}

	public void withhold(PublicCompanyI company)
	{
		moveLeftOrDown(company);
	}

	public void sell(PublicCompanyI company, int numberOfSpaces)
	{
		moveDown(company, numberOfSpaces);
	}

	public void soldOut(PublicCompanyI company)
	{
		moveUp(company);
	}

	public void moveUp(PublicCompanyI company)
	{
		StockSpaceI oldsquare = company.getCurrentPrice();
		StockSpaceI newsquare = oldsquare;
		int row = oldsquare.getRow();
		int col = oldsquare.getColumn();
		if (row > 0)
		{
			newsquare = getStockSpace(row - 1, col);
		}
		else if (upOrDownRight && col < numCols - 1)
		{
			newsquare = getStockSpace(row + 1, col + 1);
		}
		prepareMove(company, oldsquare, newsquare);
	}

	protected void moveDown(PublicCompanyI company, int numberOfSpaces)
	{
		StockSpaceI oldsquare = company.getCurrentPrice();
		StockSpaceI newsquare = oldsquare;
		int row = oldsquare.getRow();
		int col = oldsquare.getColumn();

		/* Drop the indicated number of rows */
		int newrow = row + numberOfSpaces;

		/* Don't drop below the bottom of the chart */
		while (newrow >= numRows || getStockSpace(newrow, col) == null)
			newrow--;

		/*
		 * If marker landed just below a ledge, and NOT because it was bounced
		 * by the bottom of the chart, it will stay just above the ledge.
		 */
		if (getStockSpace(newrow, col).isBelowLedge()
				&& newrow == row + numberOfSpaces)
			newrow--;

		if (newrow > row)
		{
			newsquare = getStockSpace(newrow, col);
		}
		if (newsquare != oldsquare && newsquare.closesCompany())
		{
			company.setClosed();
			oldsquare.removeToken(company);
			ReportBuffer.add(company.getName() + " closes at " + newsquare.getName());
		}
		else
		{
		    prepareMove(company, oldsquare, newsquare);
		}
	}

	protected void moveRightOrUp(PublicCompanyI company)
	{
		/* Ignore the amount for now */
		StockSpaceI oldsquare = company.getCurrentPrice();
		StockSpaceI newsquare = oldsquare;
		int row = oldsquare.getRow();
		int col = oldsquare.getColumn();
		if (col < numCols - 1 && !oldsquare.isLeftOfLedge()
				&& (newsquare = getStockSpace(row, col + 1)) != null)
		{
		}
		else if (row > 0 && (newsquare = getStockSpace(row - 1, col)) != null)
		{
		}
		prepareMove(company, oldsquare, newsquare);
	}

	protected void moveLeftOrDown(PublicCompanyI company)
	{
		StockSpaceI oldsquare = company.getCurrentPrice();
		StockSpaceI newsquare = oldsquare;
		int row = oldsquare.getRow();
		int col = oldsquare.getColumn();
		if (col > 0 && (newsquare = getStockSpace(row, col - 1)) != null)
		{
		}
		else if (row < numRows - 1
				&& (newsquare = getStockSpace(row + 1, col)) != null)
		{
		}
		if (newsquare != oldsquare && newsquare.closesCompany())
		{
			company.setClosed();
			oldsquare.removeToken(company);
			ReportBuffer.add(company.getName() + LocalText.getText("CLOSES_AT") + " " + newsquare.getName());
		}
		else
		{
			prepareMove(company, oldsquare, newsquare);
		}
	}
	
	protected void prepareMove (PublicCompanyI company,
	        StockSpaceI from, StockSpaceI to) {
		// To be written to a log file in the future.
		if (from != null && from == to)
		{
			ReportBuffer.add(LocalText.getText("PRICE_STAYS_LOG", new String[] {
			        company.getName(),
			        Bank.format(from.getPrice()),
			        from.getName()
			}));
			return;
		}
		else if (from == null && to != null)
		{
		}
		else if (from != null && to != null)
		{
			ReportBuffer.add (LocalText.getText("PRICE_MOVES_LOG", new String[] {
			        company.getName(),
			        Bank.format(from.getPrice()),
			        from.getName(),
			        Bank.format(to.getPrice()),
			        to.getName()
			}));

			/* Check for rails.game closure */
			if (to.endsGame())
			{
				ReportBuffer.add(LocalText.getText("GAME_OVER"));
				gameOver = true;
			}

		}
		company.setCurrentPrice(to);
		new PriceTokenMove (company, from, to);
	}

	public void processMove(PublicCompanyI company, StockSpaceI from,
			StockSpaceI to)
	{
		// To be written to a log file in the future.
		if (from != null) from.removeToken(company);
		if (to != null) to.addToken(company);
		//company.getCurrentPriceModel().setState(to);
	}

	/**
	 * @return
	 */
	public List getStartSpaces()
	{
		return startSpaces;
	}

	/**
	 * Return start prices as an int array. Note: this array is NOT sorted.
	 * 
	 * @return
	 */
	public int[] getStartPrices()
	{
		return startPrices;
	}

	public StockSpaceI getStartSpace(int price)
	{
		Iterator it = startSpaces.iterator();
		StockSpaceI square;
		while (it.hasNext())
		{
			square = ((StockSpaceI) it.next());
			if (square.getPrice() == price)
				return square;
		}
		return null;
	}

	/**
	 * @return
	 */
	public boolean isGameOver()
	{
		return gameOver;
	}

	/* Brett's original code */

	/**
	 * @return Returns the companiesStarted.
	 */
	/*
	public ArrayList getCompaniesStarted()
	{
		return companiesStarted;
	}
	*/

	/**
	 * @return Returns the ipoPile.
	 */
	/*
	public ArrayList getIpoPile()
	{
		return ipoPile;
	}
	*/

	public PublicCertificate removeShareFromPile(PublicCertificate stock)
	{
		if (ipoPile.contains(stock))
		{
			int index = ipoPile.lastIndexOf(stock);
			stock = ipoPile.get(index);
			ipoPile.remove(index);
			return stock;
		}
		else
		{
			return null;
		}

	}

	/**
	 * @return
	 */
	public int getNumberOfColumns()
	{
		return numCols;
	}

	/**
	 * @return
	 */
	public int getNumberOfRows()
	{
		return numRows;
	}

}
