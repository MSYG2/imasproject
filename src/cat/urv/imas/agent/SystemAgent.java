/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.HarvesterInfoAgent;
import cat.urv.imas.onthology.InfoAgent;
import static com.sun.org.apache.xerces.internal.util.FeatureState.is;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private GameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the 
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }
    
    /**
     * An error message is shown in the log area of the GUI, as well as in the 
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }

        // 2. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");

        // 3. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        //4. Generate All Agents
        //create harvester coordinator agent
        UtilsAgents.createAgent(this.getContainerController(),"harvecoord", "cat.urv.imas.agent.HarvesterCoordinatorAgent", null);
        //create harvester paper coordinator agent
        UtilsAgents.createAgent(this.getContainerController(),"papercoord", "cat.urv.imas.agent.HarvesterPaperCoordinatorAgent", null);
        //create harvester glass coordinator agent 
        UtilsAgents.createAgent(this.getContainerController(),"glasscoord", "cat.urv.imas.agent.HarvesterGlassCoordinatorAgent", null);
        //create harvester plastic coordinator agent
        UtilsAgents.createAgent(this.getContainerController(),"plasticcoord", "cat.urv.imas.agent.HarvesterPlasticCoordinatorAgent", null);
        //create scout coordinator agent
        UtilsAgents.createAgent(this.getContainerController(),"scoutcoord", "cat.urv.imas.agent.ScoutCoordinatorAgent", null);
        //create scouts and harvesters
        Cell[][] map = this.game.getMap();
        int hcount = 1;
        int scount = 1;
        Object[] arguments = null;
        for(int r = 0;r < map.length;r++)
            for(int c = 0;c < map[r].length;c++)
                if(map[r][c] instanceof StreetCell && ((StreetCell)map[r][c]).isThereAnAgent()){
                    InfoAgent info = ((StreetCell)map[r][c]).getAgent();
                    if(info.getType() == AgentType.SCOUT){
                        arguments = new Object[]{r, c};
                        UtilsAgents.createAgent(this.getContainerController(),"scout"+(scount++), "cat.urv.imas.agent.ScoutAgent", arguments);
                    }
                    else if(info.getType() == AgentType.HARVESTER){
                        arguments = new Object[]{r, c, ((HarvesterInfoAgent)info).getCapacity(), ((HarvesterInfoAgent)info).getAllowedType()};
                        UtilsAgents.createAgent(this.getContainerController(),"harve"+(hcount++), "cat.urv.imas.agent.HarvesterAgent", arguments);
                    }
                }
        
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
    }
    
    public void updateGUI() {
        this.gui.updateGame();
    }

}
