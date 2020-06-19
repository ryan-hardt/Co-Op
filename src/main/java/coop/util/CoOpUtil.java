package coop.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import coop.dao.UserDao;
import coop.model.Cycle;
import coop.model.User;

public class CoOpUtil {

	public static void updateUserSession(HttpServletRequest request) {
		  Integer uid = ((User) request.getSession().getAttribute("user")).getId();
		  UserDao ud = new UserDao();
		  request.getSession().setAttribute("user", ud.getUser(uid));
	  }
	
	public static int getCycleNumber(Cycle s) {
		List<Cycle> cycles = s.getProject().getCycles();
		Collections.sort(cycles, new Comparator<Cycle>() {
			public int compare(Cycle o1, Cycle o2) {
				return o1.getStartDate().compareTo(o2.getStartDate());
			}
		});
		for (int i = 0; i < cycles.size(); i++) {
			if (s.getId() == cycles.get(i).getId()) {
				return i+1;
			}
		}
		return 1;
	}
	
	public static String toPercentage(double n, int digits){
	    return String.format("%."+digits+"f",n*100)+"%";
	}
}
