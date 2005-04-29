/*
 * Rails: an 18xx game system. Copyright (C) 2005 Brett Lentz
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package game;

import java.util.*;

public class Player implements CashHolder
{
   public static int MAX_PLAYERS = 8;
   public static int MIN_PLAYERS = 2;
   private static int[] playerStartCash = new int[MAX_PLAYERS];
   private static int[] playerCertificateLimits = new int[MAX_PLAYERS];
   private static int playerCertificateLimit = 0;
   String name = "";
   int wallet = 0;
   boolean hasPriority = false;
   boolean hasBoughtStockThisTurn = false;
   Portfolio portfolio = null;
   ArrayList companiesSoldThisTurn = new ArrayList();

   public static void setLimits(int number, int cash, int certLimit)
   {
      if (number > 1 && number <= MAX_PLAYERS)
      {
         playerStartCash[number] = cash;
         playerCertificateLimits[number] = certLimit;
      }
   }

   /**
    * Initialises each Player's parameters which depend on the number of
    * players. To be called when all Players have been added.
    *  
    */
   public static void initPlayers(Player[] players)
   {
      Player player;
      int numberOfPlayers = players.length;
      int startCash = playerStartCash[numberOfPlayers];

      // Give each player the initial cash amount
      for (int i = 0; i < numberOfPlayers; i++)
      {
         player = (Player) players[i];
         Bank.transferCash(null, player, startCash);
         Log.write("Player " + player.getName() + " receives " + startCash + ". Bank now has " + Bank.getInstance().getCash());
      }

      // Set the sertificate limit
      playerCertificateLimit = playerCertificateLimits[numberOfPlayers];
   }

   public static int getCertLimit()
   {
      return playerCertificateLimit;
   }

   public Player(String name)
   {
      this.name = name;
      portfolio = new Portfolio(name, this);
   }

   public int buyShare(Certificate share) throws NullPointerException
   {
      if(hasBoughtStockThisTurn)
         return 0;
      
      for (int i=0; i < companiesSoldThisTurn.size(); i++)
      {
         if(share.company.getName().equalsIgnoreCase(companiesSoldThisTurn.get(i).toString()))
            return 0;           
      }
         
      if(portfolio.getCertificates().size() >= playerCertificateLimit)
         return 0;
      
      try
      {
         //throws nullpointer if company hasn't started yet.
         //it's up to the UI to catch this and gracefully start the company.
         getPortfolio().buyCertificate(share, portfolio, share.getCompany().getCurrentPrice().getPrice());
      }
      catch (NullPointerException e)
      {
         throw e;
      }
      
      Game.getPlayerManager().setBoughtStockLast(this);
      hasBoughtStockThisTurn = true;
      return 1;
   }	
   public int sellShare(Certificate share)
   {
      Portfolio.sellCertificate(share, portfolio, share.getCompany().getCurrentPrice().getPrice());
      return 1;
   }

   /**
    * @return Returns the hasPriority.
    */
   public boolean hasPriority()
   {
      return hasPriority;
   }

   /**
    * @param hasPriority
    *           The hasPriority to set.
    */
   public void setHasPriority(boolean hasPriority)
   {
      this.hasPriority = hasPriority;
   }

   /**
    * @return Returns the portfolio.
    */
   public Portfolio getPortfolio()
   {
      return portfolio;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return Returns the wallet.
    */
   public int getCash()
   {
      return wallet;
   }

   public void addCash(int amount)
   {
      wallet += amount;
   }
   
   public String toString()
   {
      return "Name: " + name + " Cash: " + wallet;
   }
}