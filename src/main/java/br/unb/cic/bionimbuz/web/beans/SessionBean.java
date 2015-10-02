package br.unb.cic.bionimbuz.web.beans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import br.unb.cic.bionimbuz.exception.ServerNotReachableException;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.rest.service.RestService;

@Named
@SessionScoped
public class SessionBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private final RestService restService;
	private User loggedUser = new User();
	private boolean serverStatus;

	private User user = new User();

	public SessionBean() {
		restService = new RestService();
	}

	/**
	 * Connect to the server to execute a Login request
	 * @return redirectView
	 */
	public String login() {
		User responseUser = null;

		// ServerNotReachable Exception
		try {
			responseUser = restService.login(user);
		} catch (ServerNotReachableException e) {
			e.printStackTrace();
			
			return "server_offline";
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return "server_offline";
		}

		// If user.cpf is not null, user sent to the server was found on database and had its data retrieved back to this client
		if (responseUser.getCpf() != null) {
			loggedUser = responseUser;

			return "success";

		// Login not found 
		} else {
			user = new User();

			return "login?faces-redirect=true&error=true";
		}

	}

	/**
	 * Invalidates user session and communicates to the server
	 * 
	 * @return
	 */
	public String logout() {
		// Invalidates user session
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

		// Send a logout message to the server
		try {
			restService.logout(loggedUser);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "logout";
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(User loggedUser) {
		this.loggedUser = loggedUser;
	}

	public boolean isServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(boolean serverStatus) {
		this.serverStatus = serverStatus;
	}

}
