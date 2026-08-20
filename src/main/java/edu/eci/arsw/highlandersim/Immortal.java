package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());

    // para las pausas
    private final Object pause = new Object();
    private volatile boolean paused = false;
    private volatile boolean waiting = false; //para saber si el hilo está en pausa

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {

        while (true) {

            checkPaused(); //esperar si esta en pausa

            Immortal im;
            int myIndex = immortalsPopulation.indexOf(this);
            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void checkPaused(){
        synchronized (pause) {
            while (paused) {
                waiting = true;
                try {
                    pause.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            waiting = false;
        } 
    }

    public void pauseImmortal() {
        synchronized (pause) {
            paused = true;
        }
    }

    public boolean pausedAndWaiting() {
        synchronized (pause) {
            return paused && waiting;
        }
    }

    public void resumeImmortal() {
        synchronized (pause) {
            paused = false;
            pause.notifyAll();
        }
    }

    public void fight(Immortal i2) {
        Immortal first = this;
        Immortal second = i2;

        if (first.name.compareTo(second.name) > 0) {
            first = i2;
            second = this;
        }

        String report;
        synchronized (first) {
            synchronized (second) {
                if (i2.health > 0) {
                    i2.health -= defaultDamageValue;
                    this.health += defaultDamageValue;
                    report = "Fight: " + this + " vs " + i2 + "\n";
                } else {
                    report = this + " says:" + i2 + " is already dead!\n";
                }
            }
        }
        updateCallback.processReport(report);
    }

    public synchronized void changeHealth(int v) {
        health = v;
    }

    public synchronized int getHealth() {
        return health;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

}
