package com.smartengine.ohnomessaging.comparator;

import java.util.Comparator;

import com.smartengine.ohnomessaging.model.Contact;

public class SortContactsByName implements Comparator<Contact>{

	@Override
	public int compare(Contact contact1, Contact contact2) {
		return contact1.getDisplayName().compareTo(contact2.getDisplayName());
	}

}
