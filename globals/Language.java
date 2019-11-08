/*   Copyright: Marie-Edith Bissey, Guido Ortona - 2007
*    Dipartimento di Politiche Pubbliche e Scelte Collettive
*    Universita' del Piemonte Orientale (Italia)
*    Contact: bissey@sp.unipmn.it

*    This file is part of ALEX4.1
*
*    ALEX4.1 is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    ALEX4.1 is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with ALEX4.1; if not, write to the Free Software
*    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package globals;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Toolkit;
import java.text.*;
import java.io.*;
import java.lang.reflect.*;

import classesWrittenByOthers.*;
import gui.*;

public class Language {
	Locale currentLocale;
	Locale oldLocale;
	HashMap <String,ResourceBundle> bundles;
	HashMap <String,ResourceBundle> oldBundles;
	
	protected EventListenerList listenerList = new EventListenerList();
	
	private Language() {
		setCurrentLocale();
		resetLanguage();
	}

	static private Language _instance = null;

	synchronized static public Language getInstance() {
		if (_instance == null) {
			synchronized(Language.class) {
				if (_instance == null) {
					_instance = new Language();
				}
			}
		}
		return _instance;
	}

	public ResourceBundle getBundle(String file) {
		return bundles.get(file);
	}

	public String getString(String file,String key) {
// System.out.println("in languages - file:"+file+", key:"+key);
// System.out.println("bundle "+getBundle(file));
		return (bundles.get(file)).getString(key);
	}

	private String getNewString(String value) {
		if (value == null) {
			return new String();
		} else {
			// first get the key corresponding to value in oldBundles (iterate over all keys and files)
			// if cannot be found in oldBundles, return the current value
			Set <String> bundleKeys = oldBundles.keySet();
			Iterator <String> bk = bundleKeys.iterator();
			while (bk.hasNext()) {
				String file = bk.next();
				ResourceBundle b = oldBundles.get(file);
				Enumeration <String> enu = b.getKeys();
				// is there a key corresponding to the current value
				while (enu.hasMoreElements()) {
					String key = enu.nextElement();
					String poss = b.getString(key);
					if (poss.compareTo(value) ==0) {
						return getString(file,key);
					} 
				}
			}
			// we have found the string with minimum differences with respect to the origin
			// now need to parse it, get compound elements (their real value in "value", their index in "stringMin")
			// and re-insert them directly in the new String if numbers, otherwise look for their equivalent if string
			
			return value;
		}
	}



	// for the default language: either the machine's default, or read the first 2 characters of the default.lang file if it exists
	private void setCurrentLocale() {
		String lang = "";
		try {
			File defaultFile= new File("languages/default.lang");
			BufferedReader bf = new BufferedReader(new FileReader(defaultFile));
			lang = bf.readLine();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
System.out.println("read "+lang);
		if (lang!="") {
			lang = lang.substring(0,2);
			currentLocale = new Locale(lang);
		}
		else {
			currentLocale = Locale.getDefault();
		}
	}
	
	// for a new Locale
	public void setCurrentLocale(Locale newLocale) {
		if (currentLocale != null) {
			oldLocale = currentLocale;
			oldBundles = bundles;
		}
System.out.println("set the new Locale");
		currentLocale = newLocale;
System.out.println("reset the language");
		resetLanguage();
System.out.println("fire change event");
		fireLocaleChangeEvent(new LocaleChangeEvent(this));
	}

	
	// methods for events
	public void addLocaleChangeEventListener(LocaleChangeEventListener listener) {
		listenerList.add(LocaleChangeEventListener.class,listener);
	}

	public void removeLocaleChangeEventListener(LocaleChangeEventListener listener) {
		listenerList.remove(LocaleChangeEventListener.class,listener);
	}

	public void fireLocaleChangeEvent(LocaleChangeEvent evt) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0 ; i < listeners.length ; i+=2) {
			if (listeners[i] == LocaleChangeEventListener.class) {
				((LocaleChangeEventListener)listeners[i+1]).LocaleHasChanged(evt);
			}
		}
	}


	
	public String getCurrentLanguage() {
		return currentLocale.getLanguage();
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public Locale getOldLocale() {
		return oldLocale;
	}

	
	private void resetLanguage() {
		/*
		 * load the language files
		 */
		 Locale.setDefault(currentLocale);
System.out.println("\t in reset language, bundles");
		bundles = new HashMap <String,ResourceBundle> (); 
		bundles.put("messages",ResourceBundle.getBundle("languages/messages",currentLocale));
		bundles.put("labels",ResourceBundle.getBundle("languages/labels",currentLocale));
		bundles.put("uninominal",ResourceBundle.getBundle("languages/uninominal",currentLocale));
		bundles.put("plurinominal",ResourceBundle.getBundle("languages/plurinominal",currentLocale));		
		bundles.put("indexes",ResourceBundle.getBundle("languages/indexes",currentLocale));
		bundles.put("powerIndexes",ResourceBundle.getBundle("languages/powerIndexes",currentLocale));
		bundles.put("repPowerIndexes",ResourceBundle.getBundle("languages/repPowerIndexes",currentLocale));

		// update the default buttons of JOptionPanes
		UIManager.put("OptionPane.yesButtonText", getString("labels","yes"));
		UIManager.put("OptionPane.cancelButtonText", getString("labels","cancel"));
		UIManager.put("OptionPane.noButtonText", getString("labels","no"));
		UIManager.put("OptionPane.okButtonText", getString("labels","ok"));
  	}


	// update components of guis by looking for new values in bundles and validating the components
	private void updateComponent(Container c) {
System.out.println("\tupdating component "+c.getClass().getName());
		if (! ((c instanceof DecimalField) || (c instanceof WholeNumberField))) {

			Method[] methods = c.getClass().getMethods();
			for (int m=0;m<methods.length;++m) {
				Method method = methods[m];
				if (method.getName().compareTo("getText")==0) {
System.out.println("\tcomponent contains getText");
					try {
						String text = (String)method.invoke(c);
						String newText = getNewString(text);
System.out.println("old text <"+text+"> becomes <"+newText+">");
						Class[] paramTypes = {String.class};
						Method s = c.getClass().getMethod("setText",paramTypes);
						Object[] param = {newText};
						s.invoke(c,param);
						c.validate();
					} catch (NoSuchMethodException e) {
						System.err.println("Cannot find method");
					} catch (InvocationTargetException e) {
						System.err.println("Cannot invoke");
					} catch(IllegalAccessException e) {
						System.err.println("Cannot access");
					}
				}
				if (method.getName().compareTo("getTitle")==0) {
System.out.println("\tcomponent contains getTitle");
					try {
						String title = (String)method.invoke(c);
						String newTitle = getNewString(title);
						Class[] paramTypes = {String.class};
						Method s = c.getClass().getMethod("setTitle",paramTypes);
						Object[] param = {newTitle};
						s.invoke(c,param);
						c.validate();
					} catch (NoSuchMethodException e) {
						System.err.println("Cannot find method");
					} catch (InvocationTargetException e) {
						System.err.println("Cannot invoke");
					} catch(IllegalAccessException e) {
						System.err.println("Cannot access");
					}
				}
			}// end for
		}// end if
	}

	public void updateAllComponents(Container c) {
System.out.println("\nCURRENT COMPONENTS OF "+c.getClass().getName());
System.out.println("update");
		updateComponent(c);
		Component[] components;

		if (c instanceof JMenu) {
			JMenu m = (JMenu)c;
			components = m.getMenuComponents();	// here or somewhere else: need to update the languages available....
		} else {
				components = c.getComponents();
		}
System.out.println("get all components: there are "+components.length+" of them");
for (int i=0;i<components.length;++i){
System.out.println((components[i].getClass().getName()));
}
		for (int i = 0 ; i < components.length ; ++i) {
System.out.println("i = "+i);
			updateAllComponents((Container)components[i]);
		}
	}


	
}
