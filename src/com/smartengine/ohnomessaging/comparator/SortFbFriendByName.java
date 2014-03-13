package com.smartengine.ohnomessaging.comparator;

import java.util.Comparator;

import com.smartengine.ohnomessaging.model.Friend;

public class SortFbFriendByName implements Comparator<Friend>{

	@Override
	public int compare(Friend friend1, Friend friend2) {
		return friend1.getName().compareTo(friend2.getName());
	}

}
