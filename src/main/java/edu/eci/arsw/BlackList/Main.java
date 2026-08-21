/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.BlackList;

import java.util.List;
import java.util.logging.Logger;
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

/**
 *
 * @author hcadavid
 */
public class Main {
    
    public static void main(String a[]){
        Logger facadeLogger = Logger.getLogger(HostBlacklistsDataSourceFacade.class.getName());
        facadeLogger.setFilter(record -> record.getThrown() == null); // oculta solo los logs con excepción
        HostBlackListsValidator hblv=new HostBlackListsValidator();

        int n = 8; // Number of threads to use for the search
        
        List<Integer> blackListOcurrences=hblv.checkHost("200.24.34.55", n);
        System.out.println("The host was found in the following blacklists:"+blackListOcurrences);
        
    }
    
}
