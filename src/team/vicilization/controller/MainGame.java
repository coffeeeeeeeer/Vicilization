package team.vicilization.controller;


import team.vicilization.gameitem.*;
import team.vicilization.gamemap.*;
import team.vicilization.mechanics.*;
import team.vicilization.country.*;
import team.vicilization.util.Position;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.Timer;
import java.util.Random;


public class MainGame extends State {

    private int round;

    private MapArea mapArea;
    private UpperInfoArea upperInfoArea;
    private LowerInfoArea lowerInfoArea;

    private Vector<Country> countries;
    private Country currentPlayer;
    private Country enermy;

    private Vector<City> cities;
    private Vector<Unit> units;

    private static JButton nextRoundButton;
    private static JButton unitMoveButton;
    private static JButton unitFightButton;
    private static JButton explorerBuildCityButton;
    private static JButton constructorHarvestButton;

    private static JComboBox<BuildingType> availableBuildings;
    private static JComboBox<UnitSubType> availableUnits;

    private boolean unitSeleted;
    private boolean unitMoving;
    private Unit selectedUnit;

    private boolean citySelected;
    private City selectedCity;

    private boolean startGame[];

    private UnitSubType underProducingUnit;
    private BuildingType underProducingBuilding;

    public MainGame(MainWindow mainWindow, CountryName[] countrys) {
        super(mainWindow);
        setNextState(StateType.Gameover);

        this.initParams();
        this.initMapArea();
        this.initButtons();
        this.initLowerInfoArea();
        this.initCountries(countrys);
        this.initUpperInfoArea();
        this.initComboxes();
        this.transView();
    }

    private void nextRound() {
        this.unselectCity();
        this.unselectUnit();
        this.mapArea.mapPanel.updateMap();

        if (judgeVectory()) {
            this.mainWindow.convertToNextState(currentPlayer);
        } else {
            round++;
            this.currentPlayer.endOfCurrentRound();
            this.enermy = this.currentPlayer;
            this.currentPlayer = this.countries
                    .elementAt(round % 2);
            this.currentPlayer.readyForNewRound();
            this.synchronizeCitiesAndUnits();
            this.upperInfoArea.update();
            this.transView();
        }
    }

    private void initParams() {
        this.round = 0;
        this.unitSeleted = false;
        this.unitMoving = false;
        this.citySelected = false;
        this.selectedCity = null;
        this.selectedUnit = null;
        this.startGame = new boolean[2];
        this.startGame[0] = false;
        this.startGame[1] = false;
        this.cities = new Vector<City>();
        this.underProducingUnit = UnitSubType.NONE;
        this.underProducingBuilding = BuildingType.NONE;
    }

    private void initCountries(CountryName[] countrys) {
        this.countries = new Vector<>(2);
        this.units = new Vector<>();
        for (int i = 0; i < 2; i++) {
            Country country = new Country(countrys[i]);
            this.countries.add(country);
            this.initUnitsForOneCountry(country, i);
        }
        this.currentPlayer = this.countries.elementAt(0);
        this.enermy = this.countries.elementAt(1);
    }

    private void initUnitsForOneCountry(Country country, int order) {
        Random initPosition = new Random();
        while (true) {
            int xbias = order * GameMapConfig.MAP_WIDTH / 2;
            int ybias = order * GameMapConfig.MAP_HEIGHT / 2;
            int x = initPosition.nextInt(GameMapConfig.MAP_WIDTH / 2) + xbias;
            int y = initPosition.nextInt(GameMapConfig.MAP_HEIGHT / 2) + ybias;
            LandSquare landSquare = mapArea.at(x, y);
            if ((landSquare.getTerrainType() == TerrainType.PLAIN
                    || landSquare.getTerrainType() == TerrainType.HILL)
                    && (landSquare.getLandformType() == LandformType.GRASSLANDS
                    || landSquare.getLandformType() == LandformType.RAINFOREST
                    || landSquare.getLandformType() == LandformType.FOREST)
                    && (landSquare.getResourceType() == ResourceType.NONE)) {

                Position initPos = new Position(x, y);
                Unit explorer = new Explorer(initPos, country);
                country.addNewUnit(explorer);
                this.units.add(explorer);
                this.mapArea.addUnitInMap(explorer, initPos);
                break;
            }
        }
    }

    private void initComboxes() {
        ComboxItemListener listener = new ComboxItemListener();

        availableBuildings = new JComboBox<BuildingType>();
        availableUnits = new JComboBox<UnitSubType>();

        availableBuildings.setBounds(1440, 960, 200, 40);
        availableUnits.setBounds(1220, 960, 200, 40);

        availableBuildings.addItemListener(listener);
        availableUnits.addItemListener(listener);
    }

    private void initButtons() {
        GameButtonsListener listener = new GameButtonsListener();

        nextRoundButton = new JButton("Next Round");
        nextRoundButton.setBounds(1800, 940, 100, 100);
        nextRoundButton.addActionListener(listener);
        this.panel.add(nextRoundButton);

        unitMoveButton = new JButton("Move");
        unitMoveButton.setBounds(20, 940, 150, 25);
        unitMoveButton.addActionListener(listener);

        unitFightButton = new JButton("Fight");
        unitFightButton.setBounds(20, 965, 150, 25);
        unitFightButton.addActionListener(listener);

        explorerBuildCityButton = new JButton("Build city");
        explorerBuildCityButton.setBounds(20, 990, 150, 25);
        explorerBuildCityButton.addActionListener(listener);

        constructorHarvestButton = new JButton("Harvest");
        constructorHarvestButton.setBounds(20, 1015, 150, 25);
        constructorHarvestButton.addActionListener(listener);
    }

    private void initUpperInfoArea() {
        this.upperInfoArea = new UpperInfoArea();
        this.panel.add(upperInfoArea);
    }

    private void initLowerInfoArea() {
        this.lowerInfoArea = new LowerInfoArea();
        this.panel.add(lowerInfoArea);
    }

    private void initMapArea() {
        this.mapArea = new MapArea();
        this.panel.add(mapArea);
    }

    private void synchronizeCitiesAndUnits() {
        this.units.clear();
        this.cities.clear();
        for (Unit unit : currentPlayer.getUnits()) {
            units.add(unit);
        }
        for (Unit unit : enermy.getUnits()) {
            units.add(unit);
        }
        for (City city : currentPlayer.getCities()) {
            cities.add(city);
        }
        for (City city : enermy.getCities()) {
            cities.add(city);
        }
    }

    private boolean judgeVectory() {
        if (this.currentPlayer.judgeScienceVictory()) {
            // science victory
            return true;
        } else if (enermy.getCities().size() == 0
                && startGame[(round + 1) % 2]) {
            // war victory
            return true;
        } else if (round == 1000) {
            // time victory
            return true;
        } else {
            return false;
        }
    }

    private void selectUnit(Unit unit) {
        this.selectedCity = null;
        this.citySelected = false;
        this.unitSeleted = true;
        this.selectedUnit = unit;
        this.lowerInfoArea.showUnitInfo(unit);
        if (!selectedUnit.isMovedThisTurn()) { // 没有移动，显示移动按钮
            this.panel.add(unitMoveButton);
        }
        if (!selectedUnit.isAttackedThisTurn()
                && selectedUnit.getType() == UnitType.FIGHTER) {
            this.panel.add(unitFightButton);
        }
        if (selectedUnit.getSubType() == UnitSubType.EXPLORER) {
            this.panel.add(explorerBuildCityButton);
        }
        if (selectedUnit.getSubType() == UnitSubType.CONSTRUCTOR) {
            LandformType landformType = this.mapArea.at(
                    selectedUnit.getPosition()).getLandformType();
            if (landformType == LandformType.FOREST
                    || landformType == LandformType.RAINFOREST
                    || landformType == LandformType.MARSH) {
                this.panel.add(constructorHarvestButton);
            }
        }
        this.panel.repaint();
    }

    private void unselectUnit() {
        this.unitSeleted = false;
        this.unitMoving = false;
        try {
            this.panel.remove(unitMoveButton);
        } catch (Exception e) {
        }
        try {
            this.panel.remove(unitFightButton);
        } catch (Exception e) {
        }
        try {
            this.panel.remove(explorerBuildCityButton);
        } catch (Exception e) {
        }
        try {
            this.panel.remove(constructorHarvestButton);
        } catch (Exception e) {
        }
        this.panel.repaint();
        this.selectedUnit = null;
        this.lowerInfoArea.unshowUnitInfo();
    }

    private void buildCity() {
        this.startGame[round % 2] = true;
        Position pos = selectedUnit.getPosition();
        currentPlayer.deleteUnit(selectedUnit);
        City city = currentPlayer.buildNewCity(pos, mapArea.getMap(), enermy);
        this.cities.add(city);
        this.units.remove(selectedUnit);
        this.unselectUnit();
        this.selectCity(city);
        this.mapArea.mapPanel.updateMap();
    }

    private void selectCity(City city) {
        this.selectedUnit = null;
        this.unitSeleted = false;
        this.unitMoving = false;
        this.citySelected = true;
        this.selectedCity = city;
        this.lowerInfoArea.showCityInfo(city);
        this.showSelectProduce();
        this.panel.repaint();
    }

    private void unselectCity() {
        this.citySelected = false;
        this.unshowSelectProduce();
        this.panel.repaint();
        this.selectedCity = null;
        this.lowerInfoArea.unshowCityInfo();
    }

    private void showSelectProduce() {
        if (!selectedCity.isProducing()) {
            this.getAllowedBuildings();
            this.getAllowedUnits();
            this.panel.add(availableBuildings);
            this.panel.add(availableUnits);
        }
    }

    private void unshowSelectProduce() {
        try {
            this.panel.remove(availableBuildings);
        } catch (Exception e) {
        }
        try {
            this.panel.remove(availableUnits);
        } catch (Exception e) {
        }
        this.clearAllowedBuildings();
        this.clearAllowedUnits();
    }

    private void showScienceTree() {
        // TODO
    }

    private void harvest() {
        Constructor constructor = (Constructor) this.selectedUnit;
        constructor.reduceTimes();
        Position position = constructor.getPosition();
        LandSquare landSquare = this.mapArea.at(position);
        this.currentPlayer.harvestResource(position, landSquare.getLandformType());
        landSquare.harvested();
        if (constructor.getTimes() == 0) {
            this.units.remove(selectedUnit);
        }
        this.mapArea.mapPanel.updateMap();
    }

    private void transView() {
        Position position;
        if (this.currentPlayer.getCities().size() != 0) {
            position = currentPlayer.getCities().get(0).getLocation();
            this.mapArea.transViewTo(position);
        } else if (this.currentPlayer.getUnits().size() != 0) {
            position = currentPlayer.getUnits().get(0).getPosition();
            this.mapArea.transViewTo(position);
        }
    }

    private void fight(Fightable fighter, Fightable fought) {
        Fighter attacker = (Fighter) fighter;
        Fighter defencer = (Fighter) fought;
        Position attackerPos = attacker.getPosition();
        Position defencerPos = defencer.getPosition();
        int attack = attacker.getAttack();
        int defence = defencer.getDefence();
        defence += this.mapArea.at(attackerPos).getDefenceBuff();
        fighter.injure(defence);
        fought.injure(attack);
        if (fighter.isDied()) {
            units.remove((Unit) fighter);
        }
        if (fought.isDied()) {
            units.remove((Unit) fought);
        }
    }

    private void fight(Fightable fighter, City city) {
        // TODO
    }

    private void showGiant() {
        // TODO
    }

    private void recruitGiant(GiantName giant) {
        // TODO
    }

    private void getAllowedBuildings() {
        Vector<BuildingType> allowedBuilding = selectedCity.getAllowedBuildings();
        availableBuildings.addItem(BuildingType.NONE);
        for (BuildingType type : allowedBuilding) {
            availableBuildings.addItem(type);
        }
        availableBuildings.setSelectedItem(BuildingType.NONE);
    }

    private void getAllowedUnits() {
        Vector<UnitSubType> allowedUnits = selectedCity.getAllowedUnits();
        availableUnits.addItem(UnitSubType.NONE);
        for (UnitSubType type : allowedUnits) {
            availableUnits.addItem(type);
        }
        availableUnits.setSelectedItem(UnitSubType.NONE);
    }

    private void clearAllowedBuildings() {
        availableBuildings.removeAllItems();
    }

    private void clearAllowedUnits() {
        availableUnits.removeAllItems();
    }

    private void selectProduction(BuildingType type) {
        selectedCity.produce(type);
        this.unshowSelectProduce();
        this.lowerInfoArea.updateCityInfo();
        this.panel.repaint();
    }

    private void selectProduction(UnitSubType type) {
        selectedCity.produce(type);
        this.unshowSelectProduce();
        this.lowerInfoArea.updateCityInfo();
        this.panel.repaint();
    }

    private void drawAccesseble() {
        Vector<LandSquare> squares = selectedUnit.
                getAvailableLocation(this.mapArea.getMap());
        for (Unit unit : units) {
            squares.remove(this.mapArea.getMap().
                    getSquare(unit.getPosition()));
        }
        for (City city : cities) {
            squares.remove(this.mapArea.getMap().
                    getSquare(city.getLocation()));
        }
        mapArea.drawAccessableSquares(squares);
    }

    private class ComboxItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            UnitSubType unitSubType = UnitSubType.NONE;
            BuildingType buildingType = BuildingType.NONE;
            if (event.getSource() == MainGame.availableUnits
                    && (unitSubType = (UnitSubType)
                    MainGame.availableUnits.getSelectedItem()) != UnitSubType.NONE) {
                selectProduction(unitSubType);
            } else if (event.getSource() == MainGame.availableBuildings
                    && (buildingType = (BuildingType)
                    MainGame.availableBuildings.getSelectedItem()) != BuildingType.NONE) {
                selectProduction(buildingType);
            }
        }
    }

    private class GameButtonsListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == MainGame.nextRoundButton) {
                nextRound();
            } else if (event.getSource() == MainGame.unitMoveButton) {
                drawAccesseble();
                unitMoving = true;
            } else if (event.getSource() == MainGame.explorerBuildCityButton) {
                buildCity();
            } else if (event.getSource() == MainGame.constructorHarvestButton) {
                harvest();
            }
            // TODO finish with other actions
        }
    }

    private class UpperInfoArea extends JPanel {
        // show flow value, resource count ... here
        private JLabel sciencePointInfo;
        private JLabel moneyInfo;
        private JLabel roundInfo;

        private JLabel sciencePointSymbolLabel;
        private JLabel moneySymbolLabel;
        private JLabel roundSymbolLabel;

        private JLabel countryName_1;
        private JLabel countryName_2;

        private ImageIcon scienceIcon;
        private ImageIcon moneyIcon;

        public UpperInfoArea() {
            // TODO science info
            super();
            this.setLayout(null);
            this.setOpaque(true);
            this.setBounds(20, 0, 1880, 50);
            this.initIcons();
            this.initLabels();
            this.update();
        }

        public void update() {
            this.sciencePointInfo.setText(
                    String.valueOf(currentPlayer.getFlowValue().getScience()));
            this.moneyInfo.setText(
                    String.valueOf(currentPlayer.getStockValue().getMoney()) + "(+"
                            + String.valueOf(currentPlayer.getFlowValue().getMoney()) + ")");

            this.roundInfo.setText(String.valueOf(round / 2 + 1));
            if (round % 2 == 0) {
                this.countryName_1.setFont(new Font("Consolas", Font.BOLD, 25));
                this.countryName_2.setFont(new Font("Consolas", Font.PLAIN, 25));
            } else {
                this.countryName_1.setFont(new Font("Consolas", Font.PLAIN, 25));
                this.countryName_2.setFont(new Font("Consolas", Font.BOLD, 25));
            }
            this.repaint();
        }

        private void initLabels() {
            this.sciencePointInfo = new JLabel();
            this.moneyInfo = new JLabel();
            this.roundInfo = new JLabel();

            this.sciencePointInfo.setBounds(60, 0, 80, 50);
            this.moneyInfo.setBounds(210, 0, 80, 50);
            this.roundInfo.setBounds(1840, 0, 40, 25);

            this.sciencePointInfo.setFont(new Font("Consolas", Font.PLAIN, 22));
            this.moneyInfo.setFont(new Font("Consolas", Font.PLAIN, 22));

            this.sciencePointSymbolLabel = new JLabel(scienceIcon);
            this.moneySymbolLabel = new JLabel(moneyIcon);
            this.roundSymbolLabel = new JLabel("Rounds");

            this.sciencePointSymbolLabel.setOpaque(true);
            this.moneySymbolLabel.setOpaque(true);

            this.sciencePointSymbolLabel.setBounds(0, 0, 50, 50);
            this.moneySymbolLabel.setBounds(150, 0, 50, 50);
            this.roundSymbolLabel.setBounds(1780, 0, 60, 25);

            this.roundSymbolLabel.setFont(new Font("Consolas", Font.BOLD, 12));
            this.roundInfo.setFont(new Font("Consolas", Font.BOLD, 12));

            this.countryName_1 = new JLabel(countries.get(0).getCountryName().toString());
            this.countryName_2 = new JLabel(countries.get(1).getCountryName().toString());

            this.countryName_1.setBounds(1500, 0, 200, 25);
            this.countryName_2.setBounds(1500, 25, 200, 25);

            this.countryName_1.setFont(new Font("Consolas", Font.PLAIN, 25));
            this.countryName_2.setFont(new Font("Consolas", Font.PLAIN, 25));

            this.countryName_1.setOpaque(true);
            this.countryName_2.setOpaque(true);

            this.countryName_1.setBackground(CountryConfig.COLOR_OF_COUNTRY.
                    get(countries.get(0).getCountryName().toString()));
            this.countryName_2.setBackground(CountryConfig.COLOR_OF_COUNTRY.
                    get(countries.get(1).getCountryName().toString()));

            this.add(sciencePointSymbolLabel);
            this.add(moneySymbolLabel);
            this.add(roundSymbolLabel);
            this.add(sciencePointInfo);
            this.add(moneyInfo);
            this.add(roundInfo);
            this.add(countryName_1);
            this.add(countryName_2);
        }

        private void initIcons() {
            this.scienceIcon = new ImageIcon("./Resource/info/science.png");
            this.moneyIcon = new ImageIcon("./Resource/info/money.png");
        }
    }

    private class LowerInfoArea extends JPanel {
        // show selected unit / city info
        private JLabel unitTypeInfo;

        private JLabel healthInfo;
        private JLabel attackInfo;
        private JLabel defenceInfo;
        private JLabel movabilityInfo;

        private JLabel healthLabel;
        private JLabel attackLabel;
        private JLabel defenceLabel;
        private JLabel movabilityLabel;

        private ImageIcon healthIcon;
        private ImageIcon attackIcon;
        private ImageIcon defenceIcon;
        private ImageIcon movabilityIcon;

        private JLabel cityNameInfo;
        private JLabel cityFoodInfo;
        private JLabel cityProductivityInfo;
        private JLabel cityMoneyInfo;
        private JLabel cityScienceInfo;

        private JLabel cityFoodLabel;
        private JLabel cityProductivityLabel;
        private JLabel cityMoneyLabel;
        private JLabel cityScienceLabel;

        private ImageIcon foodIcon;
        private ImageIcon producticityIcon;
        private ImageIcon moneyIcon;
        private ImageIcon scienceIcon;

        private JLabel producingItemLabel;
        private JLabel progressLabel;

        public LowerInfoArea() {
            super();
            this.setBounds(180, 940, 600, 100);
            this.setLayout(null);
            this.initIcons();
            this.initLabels();

            this.setOpaque(true);
        }

        public void showUnitInfo(Unit unit) {
            this.unitTypeInfo.setText(unit.getSubType().toString());
            this.attackInfo.setText(String.valueOf(unit.getUnitInfo().getAttack()));
            this.defenceInfo.setText(String.valueOf(unit.getUnitInfo().getDefence()));
            this.healthInfo.setText(String.valueOf(unit.getHealth()));
            this.movabilityInfo.setText(String.valueOf(unit.getMobility()));
        }

        public void unshowUnitInfo() {
            this.unitTypeInfo.setText("");
            this.attackInfo.setText("");
            this.defenceInfo.setText("");
            this.healthInfo.setText("");
            this.movabilityInfo.setText("");
        }

        public void updateUnitInfo() {
            this.showUnitInfo(selectedUnit);
            this.repaint();
        }

        public void showCityInfo(City city) {
            this.attackInfo.setText(String.valueOf(city.getAttack()));
            this.defenceInfo.setText(String.valueOf(city.getDefence()));
            this.healthInfo.setText(String.valueOf(city.getHealth()));

            this.cityNameInfo.setText(city.getCityName().toString());
            this.cityFoodInfo.setText(String.valueOf(city.getFlowValue().getFood()));
            this.cityProductivityInfo.setText(String.valueOf(city.getFlowValue().getProductivity()));
            this.cityMoneyInfo.setText(String.valueOf(city.getFlowValue().getMoney()));
            this.cityScienceInfo.setText(String.valueOf(city.getFlowValue().getScience()));

            UnitSubType unitSubType = city.getProducingUnit();
            BuildingType buildingType = city.getProducingBuilding();
            String producing = "None";
            int total = 0;
            if (unitSubType != UnitSubType.NONE) {
                producing = unitSubType.toString();
                total = GameItemConfig.UNIT_PRODUCTIVITY_COST.get(unitSubType);
            } else if (buildingType != BuildingType.NONE) {
                producing = buildingType.toString();
                total = GameItemConfig.BUILDING_PRODUCTIVITY_COST.get(buildingType);
            }
            int progress = city.getStockValue().getProductivity();
            this.producingItemLabel.setText(producing);
            this.progressLabel.setText(progress + " / " + total);
        }

        public void updateCityInfo() {
            this.showCityInfo(selectedCity);
            this.repaint();
        }

        public void unshowCityInfo() {
            this.attackInfo.setText("");
            this.defenceInfo.setText("");
            this.healthInfo.setText("");

            this.cityNameInfo.setText("");
            this.cityFoodInfo.setText("");
            this.cityProductivityInfo.setText("");
            this.cityMoneyInfo.setText("");
            this.cityScienceInfo.setText("");

            this.producingItemLabel.setText("");
            this.progressLabel.setText("");
        }

        private void initIcons() {
            this.healthIcon = new ImageIcon("./Resource/unitinfo/health.png");
            this.attackIcon = new ImageIcon("./Resource/unitinfo/attack.png");
            this.defenceIcon = new ImageIcon("./Resource/unitinfo/defence.png");
            this.movabilityIcon = new ImageIcon("./Resource/unitinfo/movability.png");
            this.foodIcon = new ImageIcon("./Resource/info/food.png");
            this.producticityIcon = new ImageIcon("./Resource/info/productivity.png");
            this.moneyIcon = new ImageIcon("./Resource/info/money.png");
            this.scienceIcon = new ImageIcon("./Resource/info/science.png");
        }

        private void initLabels() {
            this.attackLabel = new JLabel(attackIcon);
            this.defenceLabel = new JLabel(defenceIcon);
            this.healthLabel = new JLabel(healthIcon);
            this.movabilityLabel = new JLabel(movabilityIcon);

            this.attackLabel.setBounds(0, 20, 40, 40);
            this.defenceLabel.setBounds(0, 60, 40, 40);
            this.healthLabel.setBounds(100, 20, 40, 40);
            this.movabilityLabel.setBounds(100, 60, 40, 40);

            this.unitTypeInfo = new JLabel();
            this.healthInfo = new JLabel();
            this.attackInfo = new JLabel();
            this.defenceInfo = new JLabel();
            this.movabilityInfo = new JLabel();

            this.unitTypeInfo.setBounds(0, 0, 190, 20);
            this.attackInfo.setBounds(50, 20, 40, 40);
            this.defenceInfo.setBounds(50, 60, 40, 40);
            this.healthInfo.setBounds(150, 20, 40, 40);
            this.movabilityInfo.setBounds(150, 60, 40, 40);

            this.cityNameInfo = new JLabel();
            this.cityFoodInfo = new JLabel();
            this.cityProductivityInfo = new JLabel();
            this.cityMoneyInfo = new JLabel();
            this.cityScienceInfo = new JLabel();

            this.cityNameInfo.setBounds(250, 0, 190, 20);
            this.cityFoodInfo.setBounds(300, 20, 40, 40);
            this.cityProductivityInfo.setBounds(300, 60, 40, 40);
            this.cityMoneyInfo.setBounds(400, 20, 40, 40);
            this.cityScienceInfo.setBounds(400, 60, 40, 40);

            this.cityFoodLabel = new JLabel(foodIcon);
            this.cityProductivityLabel = new JLabel(producticityIcon);
            this.cityMoneyLabel = new JLabel(moneyIcon);
            this.cityScienceLabel = new JLabel(scienceIcon);

            this.cityFoodLabel.setBounds(250, 20, 40, 40);
            this.cityProductivityLabel.setBounds(250, 60, 40, 40);
            this.cityMoneyLabel.setBounds(350, 20, 40, 40);
            this.cityScienceLabel.setBounds(350, 60, 40, 40);

            this.producingItemLabel = new JLabel();
            this.progressLabel = new JLabel();

            this.producingItemLabel.setBounds(450, 20, 150, 40);
            this.progressLabel.setBounds(450, 65, 150, 20);

            this.progressLabel.setOpaque(true);
            this.producingItemLabel.setOpaque(true);
            this.progressLabel.setBackground(Color.RED);
            this.producingItemLabel.setBackground(Color.BLUE);

            this.unitTypeInfo.setFont(new Font("Consolas", Font.BOLD, 20));
            this.attackInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.defenceInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.healthInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.movabilityInfo.setFont(new Font("Consolas", Font.PLAIN, 18));

            this.cityNameInfo.setFont(new Font("Consolas", Font.BOLD, 20));
            this.cityFoodInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.cityProductivityInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.cityMoneyInfo.setFont(new Font("Consolas", Font.PLAIN, 18));
            this.cityScienceInfo.setFont(new Font("Consolas", Font.PLAIN, 18));

            this.producingItemLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            this.progressLabel.setFont(new Font("Consolas", Font.PLAIN, 15));

            this.add(attackLabel);
            this.add(defenceLabel);
            this.add(healthLabel);
            this.add(movabilityLabel);

            this.add(unitTypeInfo);
            this.add(healthInfo);
            this.add(attackInfo);
            this.add(defenceInfo);
            this.add(movabilityInfo);

            this.add(cityNameInfo);
            this.add(cityFoodInfo);
            this.add(cityProductivityInfo);
            this.add(cityMoneyInfo);
            this.add(cityScienceInfo);

            this.add(cityFoodLabel);
            this.add(cityProductivityLabel);
            this.add(cityMoneyLabel);
            this.add(cityScienceLabel);

            this.add(producingItemLabel);
            this.add(progressLabel);
        }
    }

    private class MapArea extends JPanel {

        private MapPanel mapPanel;

        public MapArea() {
            super();

            this.setBounds(20, 50, 1880, 890);
            this.setBackground(Color.BLUE);
            this.setLayout(null);

            mapPanel = new MapPanel();
            this.add(mapPanel);
        }

        public void addUnitInMap(Unit unit, Position position) {
            this.mapPanel.addUnit(unit, position);
        }

        public LandSquare at(int x, int y) {
            return mapPanel.map.getSquare(x, y);
        }

        public LandSquare at(Position position) {
            return mapPanel.map.getSquare(position);
        }

        public void drawAccessableSquares(Vector<LandSquare> squares) {
            this.mapPanel.drawAccessableSquares(squares);
        }

        public void drawCityTerritory() {
            this.mapPanel.drawTerritory();
        }

        public void transViewTo(Position position) {
            int x = position.getX() * 50;
            int y = position.getY() * 50;
            int deltax = 940 - x;
            int deltay = 445 - y;
            this.mapPanel.setLocation(deltax, deltay);
        }

        public GameMap getMap() {
            return this.mapPanel.map;
        }

        private class MapPanel extends JPanel {
            private Position bias;
            private GameMap map;

            private ImageIcon hill_desert_icon;
            private ImageIcon hill_frozenground_icon;
            private ImageIcon hills_forest_icon;
            private ImageIcon hills_grasslands_icon;
            private ImageIcon hills_rainforest_icon;
            private ImageIcon lake_icon;
            private ImageIcon plain_desert_icon;
            private ImageIcon plain_forest_icon;
            private ImageIcon plain_frozenground_icon;
            private ImageIcon plain_grasslands_icon;
            private ImageIcon plain_marsh_icon;
            private ImageIcon plain_rainforest_icon;
            private ImageIcon ridge_icon;
            private ImageIcon river_col_icon;
            private ImageIcon river_ne_icon;
            private ImageIcon river_nw_icon;
            private ImageIcon river_row_icon;
            private ImageIcon river_se_icon;
            private ImageIcon river_sw_icon;

            private ImageIcon constructor_icon;
            private ImageIcon explorer_icon;
            private ImageIcon footman_icon;

            private ImageIcon cityIcon;

            Vector<LandSquare> accessableSquares;

            public MapPanel() {
                super();
                this.map = new GameMap();
                this.setLayout(null);

                this.initIcons();
                this.setBounds(0, 0,
                        GameMapConfig.MAP_WIDTH * 50,
                        GameMapConfig.MAP_HEIGHT * 50);

                MapMouseEventListener dragScreen = new MapMouseEventListener();
                this.addMouseListener(dragScreen);
                this.addMouseMotionListener(dragScreen);

                this.initSquares();
                this.drawMapWithoutUnits();
                this.repaint();
            }

            private void initIcons() {
                this.hill_desert_icon = new ImageIcon("./Resource/terrain/hill_desert.png");
                this.hill_frozenground_icon = new ImageIcon("./Resource/terrain/hill_frozenground.png");
                this.hills_forest_icon = new ImageIcon("./Resource/terrain/hills_forest.png");
                this.hills_grasslands_icon = new ImageIcon("./Resource/terrain/hills_grasslands.png");
                this.hills_rainforest_icon = new ImageIcon("./Resource/terrain/hills_rainforest.png");
                this.lake_icon = new ImageIcon("./Resource/terrain/lake.png");
                this.plain_desert_icon = new ImageIcon("./Resource/terrain/plain_desert.png");
                this.plain_forest_icon = new ImageIcon("./Resource/terrain/plain_forest.png");
                this.plain_frozenground_icon = new ImageIcon("./Resource/terrain/plain_frozenground.png");
                this.plain_grasslands_icon = new ImageIcon("./Resource/terrain/plain_grasslands.png");
                this.plain_marsh_icon = new ImageIcon("./Resource/terrain/plain_marsh.png");
                this.plain_rainforest_icon = new ImageIcon("./Resource/terrain/plain_rainforest.png");
                this.ridge_icon = new ImageIcon("./Resource/terrain/ridge.png");
                this.river_col_icon = new ImageIcon("./Resource/terrain/river_col.png");
                this.river_ne_icon = new ImageIcon("./Resource/terrain/river_ne.png");
                this.river_nw_icon = new ImageIcon("./Resource/terrain/river_nw.png");
                this.river_row_icon = new ImageIcon("./Resource/terrain/river_row.png");
                this.river_se_icon = new ImageIcon("./Resource/terrain/river_se.png");
                this.river_sw_icon = new ImageIcon("./Resource/terrain/river_sw.png");

                this.constructor_icon = new ImageIcon("./Resource/unit/constructor.png");
                this.explorer_icon = new ImageIcon("./Resource/unit/explorer.png");
                this.footman_icon = new ImageIcon("./Resource/unit/footman.png");

                this.cityIcon = new ImageIcon("./Resource/city/city.png");
            }

            private void initSquares() {
                for (int i = 0; i < GameMapConfig.MAP_HEIGHT; i++) {
                    for (int j = 0; j < GameMapConfig.MAP_WIDTH; j++) {
                        JLabel square = new JLabel();
                        square.setOpaque(true);
                        square.setBounds(
                                j * 50, i * 50,
                                50, 50);
                        this.add(square);
                    }
                }
            }

            public void drawAccessableSquares(Vector<LandSquare> squares) {
                accessableSquares = squares;
                for (LandSquare landSquare : accessableSquares) {
                    Position position = landSquare.getPosition();
                    JLabel square = (JLabel) getComponentAt(
                            position.getX() * 50, position.getY() * 50);
                    square.setIcon(null);
                    square.setBackground(Color.GREEN);
                }
                this.repaint();
            }

            public void drawTerritory() {
                Vector<LandSquare> territory = selectedCity.getTerritory();
                Position center = selectedCity.getLocation();
                for (LandSquare landSquare : territory) {
                    Position position = landSquare.getPosition();
                    if (position.equals(center)) {
                        continue;
                    }
                    JLabel square = (JLabel) getComponentAt(
                            position.getX() * 50, position.getY() * 50);
                    square.setIcon(null);
                    square.setBackground(Color.GREEN);
                }
                this.repaint();
            }

            private void drawMapWithoutUnits() {
                for (int i = 0; i < GameMapConfig.MAP_HEIGHT; i++) {
                    for (int j = 0; j < GameMapConfig.MAP_WIDTH; j++) {
                        JLabel square = (JLabel) getComponentAt(
                                j * 50, i * 50);
                        square.setBackground(Color.WHITE);
                        switch (map.getSquare(j, i).getTerrainType()) {
                            case HILL:
                                switch (map.getSquare(j, i).getLandformType()) {
                                    case DESERT:
                                        square.setIcon(hill_desert_icon);
                                        break;
                                    case FROZENGROUND:
                                        square.setIcon(hill_frozenground_icon);
                                        break;
                                    case FOREST:
                                        square.setIcon(hills_forest_icon);
                                        break;
                                    case GRASSLANDS:
                                        square.setIcon(hills_grasslands_icon);
                                        break;
                                    case RAINFOREST:
                                        square.setIcon(hills_rainforest_icon);
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case LAKE:
                                square.setIcon(lake_icon);
                                break;
                            case PLAIN:
                                switch (map.getSquare(j, i).getLandformType()) {
                                    case DESERT:
                                        square.setIcon(plain_desert_icon);
                                        break;
                                    case FOREST:
                                        square.setIcon(plain_forest_icon);
                                        break;
                                    case FROZENGROUND:
                                        square.setIcon(plain_frozenground_icon);
                                        break;
                                    case GRASSLANDS:
                                        square.setIcon(plain_grasslands_icon);
                                        break;
                                    case MARSH:
                                        square.setIcon(plain_marsh_icon);
                                        break;
                                    case RAINFOREST:
                                        square.setIcon(plain_rainforest_icon);
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case RIDGE:
                                square.setIcon(ridge_icon);
                                break;
                            case RIVER_COL:
                                square.setIcon(river_col_icon);
                                break;
                            case RIVER_NE:
                                square.setIcon(river_ne_icon);
                                break;
                            case RIVER_NW:
                                square.setIcon(river_nw_icon);
                                break;
                            case RIVER_ROW:
                                square.setIcon(river_row_icon);
                                break;
                            case RIVER_SE:
                                square.setIcon(river_se_icon);
                                break;
                            case RIVER_SW:
                                square.setIcon(river_sw_icon);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            public void updateMap() {
                drawMapWithoutUnits();
                for (City city : cities) {
                    Position position = city.getLocation();
                    JLabel square = (JLabel) getComponentAt(
                            position.getX() * 50, position.getY() * 50);
                    square.setIcon(cityIcon);
                    square.setBackground(CountryConfig.COLOR_OF_COUNTRY
                            .get(city.getCountry().getCountryName()));
                }
                for (Unit unit : units) {
                    Position position = unit.getPosition();
                    JLabel square = (JLabel) getComponentAt(
                            position.getX() * 50, position.getY() * 50);
                    switch (unit.getSubType()) {
                        case EXPLORER:
                            square.setIcon(explorer_icon);
                            break;
                        case CONSTRUCTOR:
                            square.setIcon(constructor_icon);
                            break;
                        case FOOTMAN:
                            square.setIcon(footman_icon);
                            break;
                        // TODO add other
                        default:
                            break;
                    }
                    square.setBackground(CountryConfig.COLOR_OF_COUNTRY
                            .get(unit.getCountry().getCountryName()));
                }
                if (citySelected) {
                    drawTerritory();
                }
                this.repaint();
            }

            private void addUnit(Unit unit, Position position) {
                JLabel square = (JLabel) getComponentAt(
                        position.getX() * 50, position.getY() * 50);
                switch (unit.getSubType()) {
                    case EXPLORER:
                        square.setIcon(explorer_icon);
                        break;
                    case CONSTRUCTOR:
                        square.setIcon(constructor_icon);
                        break;
                    case FOOTMAN:
                        square.setIcon(footman_icon);
                        break;
                    // TODO add other
                    default:
                        break;
                }
                square.setBackground(CountryConfig.COLOR_OF_COUNTRY
                        .get(unit.getCountry().getCountryName()));
            }

            private class MapMouseEventListener implements MouseInputListener {

                private boolean moving = false;
                private int xinit = 0;
                private int yinit = 0;
                private int xcur = 0;
                private int ycur = 0;
                Timer timer;

                @Override
                public void mouseClicked(MouseEvent event) {
                    JLabel square = (JLabel) getComponentAt(event.getX(), event.getY());

                    int posx = event.getX() / 50;
                    int posy = event.getY() / 50;
                    if (!unitMoving) { // 单位没有准备移动
                        boolean found = false;
                        for (Unit u : units) {
                            if (u.getPosition().equals(new Position(posx, posy))) {
                                if (u.getCountry() == currentPlayer) {
                                    found = true;
                                    unselectCity();
                                    selectUnit(u);
                                    break;
                                }
                            }
                        }
                        for (City c : cities) {
                            if (c.getLocation().equals(new Position(posx, posy))) {
                                if (c.getCountry() == currentPlayer) {
                                    found = true;
                                    unselectUnit();
                                    selectCity(c);
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            unselectUnit();
                            unselectCity();
                        }
                    } else { // 单位移动
                        Position position = new Position(posx, posy);
                        LandSquare landSquare = map.getSquare(posx, posy);
                        if (accessableSquares.contains(landSquare)) {
                            selectedUnit.moveTo(position);
                        }
                        unselectUnit();
                    }
                    updateMap();
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                }

                @Override
                public void mouseExited(MouseEvent event) {
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    xinit = event.getX();
                    yinit = event.getY();
                    timer = new Timer(20, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            mapPanel.setLocation(
                                    xcur - xinit + mapPanel.getX(),
                                    ycur - yinit + mapPanel.getY());
                        }
                    });
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (moving) {
                        int x1 = event.getX();
                        int y1 = event.getY();
                        mapPanel.setLocation(
                                xcur - xinit + mapPanel.getX(),
                                ycur - yinit + mapPanel.getY());
                        moving = false;
                        timer.stop();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    xcur = event.getX();
                    ycur = event.getY();
                    moving = true;
                    timer.start();
                }

                @Override
                public void mouseMoved(MouseEvent event) {
                }
            }
        }
    }
}
