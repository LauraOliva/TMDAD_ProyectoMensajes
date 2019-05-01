package com.tmdad.censura;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmdad.censura.model.MensajeCensurado;
import com.tmdad.censura.model.MensajesRepository;
import com.tmdad.censura.model.PalabraCensurada;
import com.tmdad.censura.model.PalabrasRepository;

@RestController
public class CensuraController {

	@Autowired public PalabrasRepository wordRepository;
	@Autowired public MensajesRepository msgRepository;
	
	@PostMapping("/censureFilter")
    public String filter(@RequestParam("msg") String msg, @RequestParam("sender") String sender) {
    	
		Iterable<PalabraCensurada> palabras = wordRepository.findAll();
    	Iterator<PalabraCensurada> iterator = palabras.iterator();
    	boolean censored = false;
    	ArrayList<String> censored_words = new ArrayList<String>();
    	while(iterator.hasNext()){
    		PalabraCensurada p = iterator.next();
    		if(msg.contains(p.getPalabra())){
    			censored = true;
    			censored_words.add(p.getPalabra());
    			msg = msg.replaceAll(p.getPalabra(), "****");
    		}
    	}
    	
    	if(censored){
    		MensajeCensurado m = new MensajeCensurado(sender, msg, censored_words.toString(), (new Date()).getTime());
    		msgRepository.save(m);
    	}

        return msg;
    }
	
	@PostMapping("/addCensure")
    public String addCensure(@RequestParam("word") String word) {
		List<PalabraCensurada> w = wordRepository.findByWord(word);
        if(w == null || w.isEmpty()){
        	PalabraCensurada p = new PalabraCensurada(word, (new Date()).getTime());
            wordRepository.save(p);
            return "La palabra " + word + " se ha anyadido correctamente";
        }
        else{
        	return "La palabra " + word + " ya esta censurada";
        }
        
    }
	
	@PostMapping("/removeCensure")
    public String removeCensure(@RequestParam("word") String word) {
    	
		List<PalabraCensurada> p = wordRepository.findByWord(word);
        if(p != null && !p.isEmpty()){
        	wordRepository.delete(p.get(0));
            return "La palabra " + word + " se ha eliminado correctamente";
        }
        return "La palabra " + word + " no esta censurada"; 
        
    }

    @GetMapping("/censureWords")
    public String getWords() {
    	ArrayList<String> words = new ArrayList<>();
    	Iterable<PalabraCensurada> palabras = wordRepository.findAll();
    	Iterator<PalabraCensurada> iterator = palabras.iterator();
    	while(iterator.hasNext()){
    		PalabraCensurada p = iterator.next();
    		words.add(p.getPalabra());
    	}
    	
    	return words.toString();
    
        
    }
}
