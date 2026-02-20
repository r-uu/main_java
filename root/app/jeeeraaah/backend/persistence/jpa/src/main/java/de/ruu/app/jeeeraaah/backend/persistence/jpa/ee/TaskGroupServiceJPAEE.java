package de.ruu.app.jeeeraaah.backend.persistence.jpa.ee;

import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupRepositoryJPA;
import de.ruu.app.jeeeraaah.backend.persistence.jpa.TaskGroupServiceJPA;
import de.ruu.app.jeeeraaah.common.api.domain.flat.TaskGroupFlat;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Objects.nonNull;

@ApplicationScoped
@Transactional // jakarta.transaction.Transactional interceptor
@Slf4j
public class TaskGroupServiceJPAEE extends TaskGroupServiceJPA
{
	@Inject private TaskGroupRepositoryJPAEE repository;

	@PostConstruct private void postConstruct() { log.debug("repository available: {}", nonNull(repository)); }

	@Override protected TaskGroupRepositoryJPA repository() { return repository; }
}