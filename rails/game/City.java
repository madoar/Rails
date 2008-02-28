/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/City.java,v 1.1 2008/02/28 21:43:49 evos Exp $ */
package rails.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rails.game.move.Moveable;


/**
 * A City object represents any junction on the map that is relevant
 * for establishing train run length and revenue calculation.
 * A City object is bound to (1) a MapHex, (2) to a Station
 * object on the current Tile laid on that MapHex,
 * and (3) any tokens laid on that tile and station.
 * <p>
 * Each City has a unique ID, that is derived from the MapHex
 * name and the City number. The initial City numbers are derived from
 * the Station numbers of the preprinted tile of that hex.
 * <p>
 * Please note, that during upgrades the Station numbers
 * related to a city on a multiple-city hex may change:
 * city 1 on one tile may be numbered 2 on its upgrade,
 * depending on the rotation of the upgrading tile.
 * However, the City numbers will not change,
 * unless cities are merged during upgrades; but even then
 * it is attempted to retain the old city numbers as
 * much as possible.
 *
 * @author Erik Vos
 */
public class City implements TokenHolderI
{
    private int number;
	private String uniqueId;
	private Station relatedStation;
	private int slots;
	private ArrayList<TokenI> tokens;
	private MapHex mapHex;
	private String trackEdges;

	protected static Logger log = Logger.getLogger(City.class.getPackage().getName());

	public City(MapHex mapHex, int number, Station station)
	{
	    this.mapHex = mapHex;
		this.number = number;
		this.relatedStation = station;

		uniqueId = mapHex.getName()+"_"+number;
		slots = relatedStation.getBaseSlots();

		tokens = new ArrayList<TokenI>(slots);
	}

	public String getName() {
	    return "City "+number+" on Hex "+mapHex.getName();
	}

    /**
     * @return Returns the holder.
     */
    public Object getHolder() {
        return mapHex;
    }

	public int getNumber() {
        return number;
    }

    public Station getRelatedStation() {
        return relatedStation;
    }

    public void setRelatedStation(Station relatedStation) {
        this.relatedStation = relatedStation;
        trackEdges = mapHex.getConnectionString(mapHex.getCurrentTile(),
        		mapHex.getCurrentTileRotation(), relatedStation.getNumber());
    }
    
    public void setSlots (int slots) {
    	this.slots = slots;
    }

    /**
	 * @return Returns the id.
	 */
	public String getUniqueId()
	{
		return uniqueId;
	}

	public boolean addToken (TokenI token) {

	    if (tokens.contains(token)) {
	        return false;
	    } else {
		    token.setHolder(this);
	        boolean result = tokens.add(token);
	        //mapHex.update();
	        return result;
	    }
	}

	public boolean addObject (Moveable object) {
	    if (object instanceof TokenI) {
	        return addToken ((TokenI)object);
	    } else {
	        return false;
	    }
	}

    public boolean removeObject (Moveable object) {
        if (object instanceof TokenI) {
            return removeToken ((TokenI)object);
        } else {
            return false;
        }
    }

	public List<TokenI> getTokens()
	{
		return tokens;
	}

	public boolean hasTokens()
	{
		return tokens.size() > 0;
	}


	public int getSlots() {
        return slots;
    }

    public boolean hasTokenSlotsLeft() {
    	//log.debug("---Hex "+mapHex.getName()+" city "+number+" has "+slots+" slots and "+tokens.size()+" tokens");
	    return tokens.size() < slots;
	}

	public boolean removeToken (TokenI token) {

	    boolean result = tokens.remove(token);
        //mapHex.update();
	    return result;
	}

	/**
	 * @param company
	 * @return true if this City already contains an instance of the
	 *         specified company's token.
	 */
	public boolean hasTokenOf(PublicCompanyI company)
	{
		if (tokens.contains(company))
			return true;
		return false;
	}

	public void setTokens(ArrayList<TokenI> tokens)
	{
		this.tokens = tokens;
	}

	@Override
    public String toString()
	{
		return "City ID: " + number + ", Hex: " + mapHex.getName();
	}
}
