package edu.eci.arsw.BlackList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackListSearchThread extends Thread {
    private int start;
    private int end;
    private String ipAdd;
    private HostBlacklistsDataSourceFacade validator;
    private LinkedList<Integer> blackListOcurrences = new LinkedList<>();
    
    private int checkCount = 0;

    private final AtomicInteger sharedCount;
    private final int alarmCount;

    public BlackListSearchThread(int start, 
                                int end, 
                                String ipAdd, 
                                HostBlacklistsDataSourceFacade validator,
                                AtomicInteger sharedCount, 
                                int alarmCount
    ) {
        this.start = start;
        this.end = end;
        this.ipAdd = ipAdd;
        this.validator = validator;
        this.sharedCount = sharedCount;
        this.alarmCount = alarmCount;
    }

    @Override
    public void run() {
        for (int i = start; i < end; i++) {
            // Check if the shared count has reached the alarm count
            if (sharedCount.get() >= alarmCount || isInterrupted()) {
                break;
            }
            checkCount++;
            if (validator.isInBlackListServer(i, ipAdd)) {
                blackListOcurrences.add(i);
                int current = sharedCount.incrementAndGet(); // atomic increment of the shared count
                if (current >= alarmCount) {
                    break; //this thread can stop searching if the alarm count is reached
                }
            }
        }
    }

    public int getCount() {
        return blackListOcurrences.size();
    }

    public int getCheckedCount() {
        return checkCount;
    }

    public LinkedList<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
