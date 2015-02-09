package com.mportal.team.myteam.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samratsen on 2/5/15.
 */
public class ContactsStaticDataModel
{
    private static List<ContactModel> contacts = new ArrayList<ContactModel>();
    private static ContactModel logInUser = new ContactModel();

    public static ContactModel getLogInUser(){
        return logInUser;
    }

    public static void setLogInUser(ContactModel contactModel){
        logInUser = contactModel;
    }

    public static List<ContactModel> getContacts(){

        if (contacts.isEmpty()) {

            ContactModel contact2 = new ContactModel();
            contact2.setName("Samrat Mportal");
            contact2.setIdTag("ssen@mportal.com");
            contacts.add(contact2);

            ContactModel contact3 = new ContactModel();
            contact3.setName("Alec Mportal");
            contact3.setIdTag("awalker@mportal.com");
            contacts.add(contact3);
         }

        return contacts;
    }

    public static void addContact(ContactModel model){
        if (model==null||model.getIdTag()==null||model.getIdTag().isEmpty()) return;
        if (logInUser!=null && logInUser.getIdTag()!=null && model.getIdTag().trim().equalsIgnoreCase(logInUser.getIdTag().trim())){
            return;
        }
        for (ContactModel contactModel: contacts){
            if (contactModel.getIdTag().equalsIgnoreCase(model.getIdTag().trim())){
                contacts.remove(contactModel);
                contacts.add(model);
                return;
            }
        }
        contacts.add(model);
    }

    public static ContactModel getContactByIdTag(String idTag){
        if (contacts==null||idTag==null) return null;
        for (ContactModel contactModel: contacts){
            if (contactModel.getIdTag().equalsIgnoreCase(idTag.trim())){
                return contactModel;
            }
        }
        return null;
    }
}
