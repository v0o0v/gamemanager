package net.boardrank.Boardgame.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Entity
public class BoardGame {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

}