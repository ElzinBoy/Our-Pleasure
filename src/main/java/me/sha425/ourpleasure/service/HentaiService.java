package me.sha425.ourpleasure.service;

import me.sha425.ourpleasure.db.repository.HentaiRepository;
import me.sha425.ourpleasure.dto.HentaiEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HentaiService {
    @Autowired
    private HentaiRepository repository;

    public HentaiEntity addNewEntry(HentaiEntity hentaiEntity) {
        return repository.save(hentaiEntity);
    }
}
