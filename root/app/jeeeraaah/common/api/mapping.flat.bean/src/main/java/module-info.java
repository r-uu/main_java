/**
 * <h2>Mapping: Flat → Bean (TaskGroupFlat to TaskGroupBean)</h2>
 * 
 * <p><b>Purpose:</b> Converts flat REST representation (TaskGroupFlat) 
 * to domain beans (TaskGroupBean).</p>
 * 
 * <h3>Dependencies:</h3>
 * <ul>
 *   <li>{@code de.ruu.app.jeeeraaah.common.api.bean} - Bean domain objects</li>
 *   <li>{@code de.ruu.app.jeeeraaah.common.api.ws.rs} - Flat REST objects</li>
 *   <li>{@code org.mapstruct} - Mapper framework</li>
 * </ul>
 * 
 * <h3>Exports:</h3>
 * <ul>
 *   <li>{@code de.ruu.app.jeeeraaah.common.api.mapping.flat.bean} - Mapper interfaces</li>
 * </ul>
 * 
 * <h3>Opens (for frameworks):</h3>
 * <ul>
 *   <li>{@code de.ruu.app.jeeeraaah.common.api.mapping.flat.bean} to {@code org.mapstruct}</li>
 * </ul>
 * 
 * @since 0.0.1
 */
module de.ruu.app.jeeeraaah.common.api.mapping.flat.bean
{
	// Export will be added when mapper is moved to this module
	// exports de.ruu.app.jeeeraaah.common.api.mapping.flat.bean;
	// opens de.ruu.app.jeeeraaah.common.api.mapping.flat.bean to org.mapstruct;

	requires transitive de.ruu.app.jeeeraaah.common.api.bean;
	requires transitive de.ruu.app.jeeeraaah.common.api.ws.rs;
	requires transitive org.mapstruct;
}
