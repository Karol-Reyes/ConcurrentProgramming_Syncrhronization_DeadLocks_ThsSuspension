/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.BlackList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int n) {
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        int ocurrencesCount=0;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();

        if (n <= 0) {
            n = 1;
        }

        if (n > totalServers) {
            n = totalServers;
        }

        int range = totalServers / n;
        int remainder = totalServers % n;

        AtomicInteger shared = new AtomicInteger(0);
        BlackListSearchThread[] threads = new BlackListSearchThread[n];

        int start = 0;
        for (int i = 0; i < n; i++) {
            int tamano = range;
            if (i < remainder) {
                tamano++;
            }
            int end = start + tamano;
            threads[i] = new BlackListSearchThread(start, end, ipaddress, skds, shared, BLACK_LIST_ALARM_COUNT);
            start = end;
        }

        for (int i = 0; i < n; i++) {
            threads[i].start();
        }

        for (int i = 0; i < n; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, null, e);
            }

            if (shared.get() >= BLACK_LIST_ALARM_COUNT) {
                for (int j = i + 1; j < n; j++) {
                    threads[j].interrupt();
                }
            }
        }

        int checkedListsCount = 0;
        for (int i = 0; i < n; i++) {
            ocurrencesCount += threads[i].getCount();
            checkedListsCount += threads[i].getCheckedCount();
            LinkedList<Integer> resultadoDelHilo = threads[i].getBlackListOcurrences();
            for (int numeroDeLista : resultadoDelHilo) {
                blackListOcurrences.add(numeroDeLista);
            }
        }
        
        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
}
