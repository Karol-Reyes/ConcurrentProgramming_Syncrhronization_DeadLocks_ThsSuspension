package edu.eci.arsw.highlandersim;

import java.util.Queue;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;
    private int defaultDamageValue;

    private final Queue<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());

    // para las pausas
    private final Object pause = new Object();
    private volatile boolean paused = false;
    private volatile boolean waiting = false;
    private volatile boolean stopped = false;

    public Immortal(String name, Queue<Immortal> immortalsPopulation, int health,
            int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        while (!stopped) {
            checkPaused();

            if (stopped) {
                return;
            }

            if (getHealth() <= 0) {
                immortalsPopulation.remove(this);
                return;
            }

            Immortal[] population = immortalsPopulation.toArray(new Immortal[0]);
            if (population.length <= 1) {
                return;
            }

            int nextFighterIndex;
            do {
                nextFighterIndex = r.nextInt(population.length);
            } while (population[nextFighterIndex] == this);

            fight(population[nextFighterIndex]);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void checkPaused() {
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

    public void stopImmortal() {
        stopped = true;
        synchronized (pause) {
            paused = false;
            pause.notifyAll();
        }
        interrupt();
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
                if (this.health > 0 && i2.health > 0) {
                    i2.health -= defaultDamageValue;
                    this.health += defaultDamageValue;
                    if (i2.health == 0) {
                        immortalsPopulation.remove(i2);
                    }
                    report = "Fight: " + this + " vs " + i2 + "\n";
                } else {
                    if (this.health <= 0) {
                        immortalsPopulation.remove(this);
                    }
                    if (i2.health <= 0) {
                        immortalsPopulation.remove(i2);
                    }
                    report = this + " cannot fight because one of the immortals is dead.\n";
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
