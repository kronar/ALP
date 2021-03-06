//Copyright 2011-2012 Lohika .  This file is part of ALP.
//
//    ALP is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    ALP is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with ALP.  If not, see <http://www.gnu.org/licenses/>.
package com.lohika.alp.flexpilot;

import java.util.List;


/**
 * The Interface FindsByChain.
 */
public interface FindsByChain {
	  
  	/**
  	 * Find element by chain.
  	 *
  	 * @param using
  	 * @return found FlexElement
  	 */
  	FlexElement findElementByChain(String using);

	  /**
  	 * Find elements by chain.
  	 *
  	 * @param using 
  	 * @return the list of found FlexElements
  	 */
  	List<FlexElement> findElementsByChain(String using);
}
