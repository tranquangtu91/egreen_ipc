/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.egreen.plc;

import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author TuTQ
 */
@SuppressWarnings({"unused", "deprecation"})
public class PLC extends Thread {
	
    public org.apache.log4j.Logger logger;
    Date date = new Date();

    private String ip = "localhost";
    private int port = Modbus.DEFAULT_PORT;
	private boolean connected = false;	

    public boolean auto_light = false;
    public int solar_radiation = 0;
    public int solar_radiation_threehold = 5;

    private int blind_roll_1_up_addr = 4;
    private int blind_roll_1_down_addr = 5;
    private int blind_roll_2_up_addr = 6;
    private int blind_roll_2_down_addr = 7;
    private boolean blind_roll_schedule_en = false;
    private int blind_roll_schedule_max = 4;
    private List<Integer> blind_roll_schedule_run_time = new ArrayList<>();
    private List<Integer> blind_roll_schedule_target_close_percent = new ArrayList<>();
    private boolean blind_roll_closing = false;
    private long blind_roll_close_wait_time;
    private int blind_roll_full_close_time = 5000;
    private int blind_roll_current_close_percent = 0;
    private int blind_roll_target_close_percent = 100;

    private int shading_screen_open_addr = 8;
    private int shading_screen_close_addr = 9;
    private boolean shading_screen_schedule_en = false;
    private int shading_screen_schedule_max = 4;
    private List<Integer> shading_screen_schedule_run_time = new ArrayList<>();
    private List<Integer> shading_screen_schedule_target_close_percent = new ArrayList<>();
    private boolean shading_screen_closing = false;
    private long shading_screen_close_wait_time;
    private int shading_screen_full_close_time = 5000;
    private int shading_screen_current_close_percent = 0;
    private int shading_screen_target_close_percent = 100;

    private int light_addr = 17;
    private boolean light_schedule_en = false;
    private int light_schedule_max = 2;
    private List<Integer> light_schedule_run_time = new ArrayList<>();
    private List<Boolean> light_schedule_target_state = new ArrayList<>();
    private Boolean light_current_state = true;
    private Boolean light_target_state = false;
    
    private class ValveScheduler {
    	public int valve_index;
    	public int start_time;
    	public int hold_time;
    	
    	public ValveScheduler(int vavle_index, int start_time, int hold_time) {
			// TODO Auto-generated constructor stub
    		this.valve_index = vavle_index;
    		this.start_time = start_time;
    		this.hold_time = hold_time;
		}
    }
    
    private int sprinker_valve_1_addr = 0;
    private Boolean sprinker_valve_1_current_state = true;
    private Boolean sprinker_valve_1_target_state = false;    
    private int sprinker_valve_2_addr = 2;
    private Boolean sprinker_valve_2_current_state = true;
    private Boolean sprinker_valve_2_target_state = false;   
    private boolean sprinker_valve_schedule_en = false;
    private int sprinker_valve_schedule_max = 4;
    private List<ValveScheduler> sprinker_valve_schedule_run_time = new ArrayList<>();
    
    private int driper_valve_1_addr = 1;
    private Boolean driper_valve_1_current_state = true;
    private Boolean driper_valve_1_target_state = false;
    private int driper_valve_2_addr = 3;
    private Boolean driper_valve_2_current_state = true;
    private Boolean driper_valve_2_target_state = false;
    private boolean driper_valve_schedule_en = false;
    private int driper_valve_schedule_max = 4;
    private List<ValveScheduler> driper_valve_schedule_run_time = new ArrayList<>();

    private int roof_1_up_addr = 24;
    private int roof_1_down_addr = 25;
    private int roof_2_up_addr = 26;
    private int roof_2_down_addr = 27;
    private int roof_3_up_addr = 28;
    private int roof_3_down_addr = 29;
    private int roof_4_up_addr = 30;
    private int roof_4_down_addr = 31;
    private int roof_5_up_addr = 32;
    private int roof_5_down_addr = 33;
    private int roof_6_up_addr = 34;
    private int roof_6_down_addr = 35;
    private int roof_7_up_addr = 36;
    private int roof_7_down_addr = 37;
    private boolean roof_schedule_en = false;
    private int roof_schedule_max = 4;
    private List<Integer> roof_schedule_run_time = new ArrayList<>();
    private List<Integer> roof_schedule_target_close_percent = new ArrayList<>();
    private boolean roof_closing = false;
    private long roof_close_wait_time;
    private int roof_full_close_time = 5000;
    private int roof_current_close_percent = 0;
    private int roof_target_close_percent = 100;

    private TCPMasterConnection con;
    private ModbusTCPTransaction trans;
    private ReadInputDiscretesRequest req2;
    private ReadInputDiscretesResponse res2;
    private int read_input_discretes_t = 5000;
    private long read_input_discretes_waittime;
    private WriteCoilRequest req3;
    private WriteCoilResponse res3;
    private ReadCoilsRequest req1;
    private ReadCoilsResponse res1;
    private int read_coils_t = 5000;
    private long read_coils_waittime;

    public PLC(String host, int port) {
        logger = org.apache.log4j.Logger.getLogger(PLC.class);
        PropertyConfigurator.configure("log4j.properties");
        
        this.ip = host;
        this.port = port;
        connected = false;
        
        req1 = new ReadCoilsRequest();
        req1.setUnitID(1);
        req1.setReference(0);
    	req1.setBitCount(5*8);
        req2 = new ReadInputDiscretesRequest();
        req2.setUnitID(1);
        req2.setReference(0);
        req2.setBitCount(2*8);
        req3 = new WriteCoilRequest();
        req3.setUnitID(1);
    }
    
    private void first_run() {
        blind_roll_current_close_percent = 0;
        blind_roll_set_target_close_percent(100);
        
        shading_screen_current_close_percent = 0;
        shading_screen_set_target_close_percent(100);
        
        roof_current_close_percent = 0;
        roof_set_target_close_percent(100);
        
        light_current_state = true;
        light_set_target_state(false);

        sprinker_valve_1_current_state = true;
        sprinker_valve_set_target_state(1,  false);
        sprinker_valve_2_current_state = true;
        sprinker_valve_set_target_state(2,  false);
        sprinker_valve_schedule_en = false;
        
        driper_valve_1_current_state = true;
        driper_valve_set_target_state(1,  false);
        driper_valve_2_current_state = true;
        driper_valve_set_target_state(2,  false);
        driper_valve_schedule_en = false;
    }
    
    @Override
    public void run() {

        while (true) {     
        	if (con == null || !connected) {
                try {
                	Thread.sleep(1000);
        			connect();
        	        first_run();
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			logger.error("Exception: " + e.getMessage());
        			continue;
        		}
        	}
        	
			try {
				roof_control();
		        shading_screen_control();
		        blind_roll_control();
		        light_control();
		        sprinker_valve_control();
		        driper_valve_control();
		        
		        read_coils();
		        read_discrete_inputs();
			} catch (ModbusIOException e) {
				// TODO Auto-generated catch block
				logger.error("ModbusIOException: " + e.getMessage());
				connected = false;
			} catch (ModbusSlaveException e) {
				// TODO Auto-generated catch block
				logger.error("ModbusSlaveException: " + e.getMessage());
			} catch (ModbusException e) {
				// TODO Auto-generated catch block
				logger.error("ModbusException: " + e.getMessage());
			} catch (NullPointerException e) {
				logger.error("NullPointerException: " + e.getMessage());
			}

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PLC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //----------------------------Modbus Proccess-------------------------------
    private void connect() throws Exception {
        logger.debug("connect " + ip);
        con = new TCPMasterConnection(InetAddress.getByName(ip));
        con.setPort(port);
        con.connect();
        trans = new ModbusTCPTransaction(con);
        connected = true;
        read_input_discretes_waittime = System.currentTimeMillis() + read_input_discretes_t;
        read_coils_waittime = System.currentTimeMillis() + read_input_discretes_t;
    }    
    
    private void write_single_coil(int address, Boolean value) throws ModbusIOException, ModbusSlaveException, ModbusException, NullPointerException {
		req3.setReference(address);
		req3.setCoil(value);
		trans.setRequest(req3);
		trans.execute();
    }
    
    private void read_coils() throws ModbusIOException, ModbusSlaveException, ModbusException {
    	if (System.currentTimeMillis() < read_coils_waittime) return;
    	read_coils_waittime = System.currentTimeMillis() + read_coils_t;
    	trans.setRequest(req1);
		trans.execute();
		res1 = (ReadCoilsResponse) trans.getResponse();
		logger.debug("read_coils: " + res1.getHexMessage());

		sprinker_valve_1_current_state = res1.getCoilStatus(sprinker_valve_1_addr);
		driper_valve_1_current_state = res1.getCoilStatus(driper_valve_1_addr);
		sprinker_valve_2_current_state = res1.getCoilStatus(sprinker_valve_2_addr);
		driper_valve_2_current_state = res1.getCoilStatus(driper_valve_2_addr);
    }
    
    private void read_discrete_inputs() throws ModbusIOException, ModbusSlaveException, ModbusException {
    	if (System.currentTimeMillis() < read_input_discretes_waittime) return;
    	read_input_discretes_waittime = System.currentTimeMillis() + read_input_discretes_t;
    	trans.setRequest(req2);
		trans.execute();
		res2 = (ReadInputDiscretesResponse) trans.getResponse();
		logger.debug("read_discrete_inputs: " + res2.getHexMessage());
    }
    //--------------------------------------------------------------------------
    
    //----------------------------Control Blind Roll----------------------------
    /**
     *
     * @param direct    0: stop; 1: up; 2: down
     * @throws ModbusException 
     * @throws ModbusSlaveException 
     * @throws ModbusIOException 
     */
    private void blind_roll(int direct) throws ModbusIOException, ModbusSlaveException, ModbusException {
        boolean blind_roll_up_state = false;
        boolean blind_roll_down_state = false;
        
        switch (direct) {
            case 1:
                blind_roll_up_state = true;
                break;
            case 2:
                blind_roll_down_state = true;
                break;
        }
        
    	write_single_coil(blind_roll_1_down_addr, blind_roll_down_state);
    	write_single_coil(blind_roll_2_down_addr, blind_roll_down_state);

    	write_single_coil(blind_roll_1_up_addr, blind_roll_up_state);
    	write_single_coil(blind_roll_2_up_addr, blind_roll_up_state);
    }
    
    public boolean blind_roll_add_schedule(int run_time, int close_percent) {
        boolean result;
        result = !blind_roll_schedule_run_time.contains(run_time);
        if (!result) return false;
        
        result = (blind_roll_schedule_run_time.size() == blind_roll_schedule_target_close_percent.size() & blind_roll_schedule_run_time.size() < blind_roll_schedule_max);
        if (!result) return result;
        result = blind_roll_schedule_run_time.add(run_time);
        if (!result) return result;
        result = blind_roll_schedule_target_close_percent.add(close_percent);
        if (!result) return result;
        
        logger.debug("add_blind_roll_schedule - Close " + close_percent + "% at " + run_time);
        return result;
    }
    
    public boolean blind_roll_set_target_close_percent(int percent) {
        if (!blind_roll_closing & blind_roll_current_close_percent != percent) {
            logger.debug("set_target_blind_roll_close_percent " + percent);
            blind_roll_target_close_percent = percent;
            return true;
        }
        return false;
    }
    
	private void blind_roll_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
        if (blind_roll_schedule_en)
        {
            for (int i = 0; i < blind_roll_schedule_run_time.size(); i++) {
                date.setTime(System.currentTimeMillis());
                int delta_time = Math.abs(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() - blind_roll_schedule_run_time.get(i));
                if (delta_time < 20) {
                    blind_roll_set_target_close_percent(blind_roll_schedule_target_close_percent.get(i));
                }
            }
        }
        
        if (blind_roll_current_close_percent == blind_roll_target_close_percent) return;
        
        if (!blind_roll_closing) {
            blind_roll_closing = true;
            int hold_time = blind_roll_full_close_time;
            if (blind_roll_target_close_percent != 100) {
                hold_time = blind_roll_full_close_time * Math.abs(blind_roll_target_close_percent - blind_roll_current_close_percent) / 100;
            }
            blind_roll_close_wait_time = System.currentTimeMillis() + hold_time;            
            logger.debug("blind_roll_close " + blind_roll_target_close_percent + "% after " + hold_time + " ms");
            
            if (blind_roll_current_close_percent > blind_roll_target_close_percent) {
                blind_roll(1);
            }
            else {
                blind_roll(2);
            }
            
        } else if (System.currentTimeMillis() > blind_roll_close_wait_time) {
            blind_roll_closing = false;
            blind_roll_current_close_percent = blind_roll_target_close_percent;
            blind_roll(0);
            logger.debug("blind_roll_close OK");
        }
    }
    
    //--------------------------------------------------------------------------
    
	//----------------------------Control Shading Screen------------------------
    /**
    *
    * @param direct    0: stop; 1: Open; 2: Close
     * @throws ModbusException 
     * @throws ModbusSlaveException 
     * @throws ModbusIOException 
    */
	private void shading_screen(int direct) throws ModbusIOException, ModbusSlaveException, ModbusException {
		boolean shading_screen_open_state = false;
		boolean shading_screen_close_state = false;
       
		switch (direct) {
			case 1:
				shading_screen_open_state = true;
				break;
			case 2:
				shading_screen_close_state = true;
				break;
		}
		write_single_coil(shading_screen_open_addr, shading_screen_open_state);
		write_single_coil(shading_screen_close_addr, shading_screen_close_state);
	}

	public boolean shading_screen_add_schedule(int run_time, int close_percent) {
		boolean result;
		result = !shading_screen_schedule_run_time.contains(run_time);
		if (!result) return false;
       
		result = (shading_screen_schedule_run_time.size() == shading_screen_schedule_target_close_percent.size() & shading_screen_schedule_run_time.size() < shading_screen_schedule_max);
		if (!result) return result;
		result = shading_screen_schedule_run_time.add(run_time);
		if (!result) return result;
		result = shading_screen_schedule_target_close_percent.add(close_percent);
		if (!result) return result;
       
		logger.debug("add_shading_screen_schedule - Close " + close_percent + "% at " + run_time);
		return result;
	}
   
	public boolean shading_screen_set_target_close_percent(int percent) {
	   	if (!shading_screen_closing & shading_screen_current_close_percent != percent) {
	   		logger.debug("set_target_shading_screen_close_percent " + percent);
	   		shading_screen_target_close_percent = percent;
	   		return true;
	   	}
	   	return false;
	}
   
	private void shading_screen_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
		if (shading_screen_schedule_en)
		{
			for (int i = 0; i < shading_screen_schedule_run_time.size(); i++) {
				date.setTime(System.currentTimeMillis());
				int delta_time = Math.abs(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() - shading_screen_schedule_run_time.get(i));
				if (delta_time < 20) {
					shading_screen_set_target_close_percent(shading_screen_schedule_target_close_percent.get(i));
				}
			}
		}
       
		if (shading_screen_current_close_percent == shading_screen_target_close_percent) return;
       
		if (!shading_screen_closing) {
			shading_screen_closing = true;
			int hold_time = shading_screen_full_close_time;
			if (shading_screen_target_close_percent != 100) {
				hold_time = shading_screen_full_close_time * Math.abs(shading_screen_target_close_percent - shading_screen_current_close_percent) / 100;
			}
			shading_screen_close_wait_time = System.currentTimeMillis() + hold_time;            
			logger.debug("shading_screen_close " + shading_screen_target_close_percent + "% after " + hold_time + " ms");
       
			if (shading_screen_current_close_percent > shading_screen_target_close_percent) {
				shading_screen(1);
			}
			else {
				shading_screen(2);
			}
       
		} else if (System.currentTimeMillis() > shading_screen_close_wait_time) {
			shading_screen_closing = false;
			shading_screen_current_close_percent = shading_screen_target_close_percent;
			shading_screen(0);
			logger.debug("shading_screen_close OK");
		}
	}
	//--------------------------------------------------------------------------
	
	//----------------------------Control Light---------------------------------
	private void light(boolean state) throws ModbusIOException, ModbusSlaveException, ModbusException {
		write_single_coil(light_addr, state);
	}
   
	public boolean light_add_schedule(int run_time, boolean state) {
		boolean result;
		result = !light_schedule_run_time.contains(run_time);
		if (!result) return false;
       
		result = (light_schedule_run_time.size() == light_schedule_target_state.size() & light_schedule_run_time.size() < light_schedule_max);
		if (!result) return result;
		result = light_schedule_run_time.add(run_time);
		if (!result) return result;
		result = light_schedule_target_state.add(state);
		if (!result) return result;
		
		return result;
	}
   
	public boolean light_set_target_state(boolean state) {
	   	if (light_current_state != state) {
	   		logger.debug("light_set_target_state " + state);
	   		light_target_state = state;
	   		return true;
	   	}
	   	return false;
	}
   
	private void light_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
		if (light_schedule_en)
		{
			for (int i = 0; i < light_schedule_run_time.size(); i++) {
				date.setTime(System.currentTimeMillis());
				int delta_time = Math.abs(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() - shading_screen_schedule_run_time.get(i));
				if (delta_time < 20) {
					light_set_target_state(light_schedule_target_state.get(i));
				}
			}
		}
		
		if (auto_light) {
			if (solar_radiation < solar_radiation_threehold) {
				light_set_target_state(true);
			} else {
				light_set_target_state(false);
			}
		}
       
		if (light_current_state != light_target_state) {		
			light(light_target_state);
			light_current_state = light_target_state;
		}
	}
	//--------------------------------------------------------------------------
	
	//----------------------------Control roof----------------------------------
	/**
    *
    * @param direct    0: stop; 1: Open; 2: Close
	 * @throws ModbusException 
	 * @throws ModbusSlaveException 
	 * @throws ModbusIOException 
    */
	private void roof(int direct) throws ModbusIOException, ModbusSlaveException, ModbusException {
        boolean roof_up_state = false;
        boolean roof_down_state = false;
        
        switch (direct) {
            case 1:
                roof_up_state = true;
                break;
            case 2:
                roof_down_state = true;
                break;
        }
        
        write_single_coil(roof_1_down_addr, roof_down_state);
        write_single_coil(roof_2_down_addr, roof_down_state);
        write_single_coil(roof_3_down_addr, roof_down_state);
        write_single_coil(roof_4_down_addr, roof_down_state);
        write_single_coil(roof_5_down_addr, roof_down_state);
        write_single_coil(roof_6_down_addr, roof_down_state);
        write_single_coil(roof_7_down_addr, roof_down_state);
        
        write_single_coil(roof_1_up_addr, roof_up_state);
        write_single_coil(roof_2_up_addr, roof_up_state);
        write_single_coil(roof_3_up_addr, roof_up_state);
        write_single_coil(roof_4_up_addr, roof_up_state);
        write_single_coil(roof_5_up_addr, roof_up_state);
        write_single_coil(roof_6_up_addr, roof_up_state);
        write_single_coil(roof_7_up_addr, roof_up_state);
    }
	
	public boolean roof_add_schedule(int run_time, int close_percent) {
        boolean result;
        result = !roof_schedule_run_time.contains(run_time);
        if (!result) return false;
        
        result = (roof_schedule_run_time.size() == roof_schedule_target_close_percent.size() & roof_schedule_run_time.size() < roof_schedule_max);
        if (!result) return result;
        result = roof_schedule_run_time.add(run_time);
        if (!result) return result;
        result = roof_schedule_target_close_percent.add(close_percent);
        if (!result) return result;
        
        logger.debug("add_roof_schedule - Close " + close_percent + "% at " + run_time);
        return result;
    }
    
    private void roof_set_target_close_percent(int percent) {
        if (!roof_closing & roof_current_close_percent != percent) {
            logger.debug("set_target_roof_close_percent " + percent);
            roof_target_close_percent = percent;
        }
    }
    
    private void roof_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
    	if (roof_schedule_en)
		{
			for (int i = 0; i < roof_schedule_run_time.size(); i++) {
				date.setTime(System.currentTimeMillis());
				int delta_time = Math.abs(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() - roof_schedule_run_time.get(i));
				if (delta_time < 20) {
					roof_set_target_close_percent(roof_schedule_target_close_percent.get(i));
				}
			}
		}
       
		if (roof_current_close_percent == roof_target_close_percent) return;
       
		if (!roof_closing) {
			roof_closing = true;
			int hold_time = roof_full_close_time;
			if (roof_target_close_percent != 100) {
				hold_time = roof_full_close_time * Math.abs(roof_target_close_percent - roof_current_close_percent) / 100;
			}
			roof_close_wait_time = System.currentTimeMillis() + hold_time;            
			logger.debug("roof_close " + roof_target_close_percent + "% after " + hold_time + " ms");
       
			if (roof_current_close_percent > roof_target_close_percent) {
				roof(1);
			}
			else {
				roof(2);
			}
       
		} else if (System.currentTimeMillis() > roof_close_wait_time) {
			roof_closing = false;
			roof_current_close_percent = roof_target_close_percent;
			roof(0);
			logger.debug("roof_close OK");
		}
    }
    //--------------------------------------------------------------------------
    
    //----------------------------Control sprinker_valve------------------------
  	private void sprinker_valve(int valve_addr, boolean state) throws ModbusIOException, ModbusSlaveException, ModbusException {
  		write_single_coil(valve_addr, state);
  	}
     
  	public boolean sprinker_valve_add_schedule(ValveScheduler run_time) {
  		boolean result;
  		result = !sprinker_valve_schedule_run_time.contains(run_time);
  		if (!result) return false;
  		result = (sprinker_valve_schedule_run_time.size() < sprinker_valve_schedule_max);
  		if (!result) return result;
  		sprinker_valve_schedule_run_time.add(run_time);
  		return result;
  	}
     
  	public boolean sprinker_valve_set_target_state(int valve_index, boolean state) {
  		switch (valve_index) {
	  		case 1:
	  			if (sprinker_valve_1_current_state != state) {
	  	  	   		logger.debug("sprinker_valve_1_set_target_state " + state);
	  	  	   		sprinker_valve_1_target_state = state;
	  	  	   		return true;
	  			}
	  			break;
	  		case 2:
	  			if (sprinker_valve_2_current_state != state) {
	  	  	   		logger.debug("sprinker_valve_2_set_target_state " + state);
	  	  	   		sprinker_valve_2_target_state = state;
	  	  	   		return true;
	  			}
	  			break;
  		}
  		return false;
  	}
     
  	private void sprinker_valve_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
  		if (sprinker_valve_schedule_en)
  		{
  			for (int i = 0; i < sprinker_valve_schedule_run_time.size(); i++) {
  				date.setTime(System.currentTimeMillis());
  				if ((date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() > sprinker_valve_schedule_run_time.get(i).start_time) && 
  						(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() < sprinker_valve_schedule_run_time.get(i).start_time + sprinker_valve_schedule_run_time.get(i).hold_time)) {
  					sprinker_valve_set_target_state(sprinker_valve_schedule_run_time.get(i).valve_index, true);
  				} else {
  					sprinker_valve_set_target_state(sprinker_valve_schedule_run_time.get(i).valve_index, false);
  				}
  			}
  		}
         
  		if (sprinker_valve_1_current_state != sprinker_valve_1_target_state) {
  			sprinker_valve(sprinker_valve_1_addr, sprinker_valve_1_target_state);
  			sprinker_valve_1_current_state = sprinker_valve_1_target_state;
  		}
  		if (sprinker_valve_2_current_state != sprinker_valve_2_target_state) {
  			sprinker_valve(sprinker_valve_2_addr, sprinker_valve_2_target_state);
  			sprinker_valve_2_current_state = sprinker_valve_2_target_state;
  		}
  	}
  	//--------------------------------------------------------------------------
  	
    //----------------------------Control driper_valve--------------------------
  	private void driper_valve(int valve_addr, boolean state) throws ModbusIOException, ModbusSlaveException, ModbusException {
  		write_single_coil(valve_addr, state);
  	}
     
  	public boolean driper_valve_add_schedule(ValveScheduler run_time) {
  		boolean result;
  		result = !driper_valve_schedule_run_time.contains(run_time);
  		if (!result) return false;
  		result = (driper_valve_schedule_run_time.size() < driper_valve_schedule_max);
  		if (!result) return result;
  		driper_valve_schedule_run_time.add(run_time);
  		return result;
  	}
     
  	public boolean driper_valve_set_target_state(int valve_index, boolean state) {
  		switch (valve_index) {
	  		case 1:
	  			if (driper_valve_1_current_state != state) {
	  	  	   		logger.debug("driper_valve_1_set_target_state " + state);
	  	  	   		driper_valve_1_target_state = state;
	  	  	   		return true;
	  			}
	  			break;
	  		case 2:
	  			if (driper_valve_2_current_state != state) {
	  	  	   		logger.debug("driper_valve_2_set_target_state " + state);
	  	  	   		driper_valve_2_target_state = state;
	  	  	   		return true;
	  			}
	  			break;
  		}
  		return false;
  	}
     
  	private void driper_valve_control() throws ModbusIOException, ModbusSlaveException, ModbusException {
  		if (driper_valve_schedule_en)
  		{
  			for (int i = 0; i < driper_valve_schedule_run_time.size(); i++) {
  				date.setTime(System.currentTimeMillis());
  				if ((date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() > driper_valve_schedule_run_time.get(i).start_time) && 
  						(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds() < driper_valve_schedule_run_time.get(i).start_time + driper_valve_schedule_run_time.get(i).hold_time)) {
  					driper_valve_set_target_state(driper_valve_schedule_run_time.get(i).valve_index, true);
  				} else {
  					driper_valve_set_target_state(driper_valve_schedule_run_time.get(i).valve_index, false);
  				}
  			}
  		}
         
  		if (driper_valve_1_current_state != driper_valve_1_target_state) {
  			driper_valve(driper_valve_1_addr, driper_valve_1_target_state);
  			driper_valve_1_current_state = driper_valve_1_target_state;
  		}
  		if (driper_valve_2_current_state != driper_valve_2_target_state) {
  			driper_valve(driper_valve_2_addr, driper_valve_2_target_state);
  			driper_valve_2_current_state = driper_valve_2_target_state;
  		}
  	}
  	//--------------------------------------------------------------------------
}
