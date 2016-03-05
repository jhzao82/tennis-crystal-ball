package org.strangeforest.tcb.dataload

import groovy.sql.*

class XMLMatchLoader extends BaseXMLLoader {

	XMLMatchLoader(Sql sql) {
		super(sql)
	}

	String loadSql() {
		'{call load_match(' +
			':ext_tournament_id, :season, :tournament_date, :tournament_name, :event_name, :tournament_level, :surface, :indoor, :draw_type, :draw_size, :rank_points, ' +
			':match_num, :round, :best_of, ' +
			':ext_winner_id, :winner_seed, :winner_entry, :winner_rank, :winner_rank_points, :winner_age, :winner_country_id, :winner_name, :winner_height, :winner_hand, ' +
			':ext_loser_id, :loser_seed, :loser_entry, :loser_rank, :loser_rank_points, :loser_age, :loser_country_id, :loser_name, :loser_height, :loser_hand, ' +
			':score, :outcome, :w_sets, :l_sets, :w_games, :l_games, :w_set_games, :l_set_games, :w_set_tb_pt, :l_set_tb_pt, :minutes, ' +
			':w_ace, :w_df, :w_sv_pt, :w_1st_in, :w_1st_won, :w_2nd_won, :w_sv_gms, :w_bp_sv, :w_bp_fc, ' +
			':l_ace, :l_df, :l_sv_pt, :l_1st_in, :l_1st_won, :l_2nd_won, :l_sv_gms, :l_bp_sv, :l_bp_fc' +
		')}'
	}

	int batch() { 1 }

	boolean loadItem(item) {
		def players = playerMap(item.player.list())
		def matches = item.match.list()
		sql.withBatch(loadSql()) { ps ->
			matches.each { match ->
				Map params = tournamentParams(item)
				params.match_num = smallint match.@'match-num'
				params.round = string match.@round
				params.best_of = smallint match.@'best-of'
				params.putAll playerParams(match, 'winner', players)
				params.putAll playerParams(match, 'loser', players)
				params.putAll scoreParams(match, sql.connection)
				ps.addBatch(params)
			}
		}
	}

	Map playerMap(players) {
		players.collectEntries { [(it.@name.toString()): playerItem(it)] }
	}

	Map playerItem(player) {
		def playerItem = [:]
		playerItem.seed = smallint player.@seed
		playerItem.entry = string player.@entry
		playerItem.name = string player.@name
		playerItem.country = country player.@country
		playerItem.rank = integer player.@rank
		playerItem.rankPoints = integer player.@'rank-points'
		playerItem.extId = integer player.@'ext-id'
		return playerItem
	}

	Map tournamentParams(tournament) {
		def params = [:]
		params.ext_tournament_id = string tournament.@'ext-id'
		params.season = smallint tournament.@season
		params.tournament_date = date tournament.@date
		def name = string tournament.@name
		params.tournament_name = name
		params.event_name = name
		params.tournament_level = string tournament.level
		params.surface = string tournament.surface
		params.indoor = bool tournament.indoor, false
		params.draw_type = string tournament.'draw-type'
		params.draw_size = smallint tournament.'draw-size'
		params.rank_points = null
		return params
	}

	Map playerParams(match, type, players) {
		def name = match['@' +type].toString()
		def player = players[name]
		def params = [:]
		params['ext_' + type + '_id'] = player?.extId
		params[type + '_seed'] = player?.seed
		params[type + '_entry'] = player?.entry
		params[type + '_rank'] = player?.rank
		params[type + '_rank_points'] = player?.rankPoints
		params[type + '_age'] = null
		params[type + '_country_id'] = player?.country
		params[type + '_name'] = player ? player.name : name
		params[type + '_height'] = null
		params[type + '_hand'] = null
		params
	}

	Map scoreParams(match, conn) {
		def params = [:]
		def score = string match.@score
		def matchScore = MatchScore.parse(score)
		params.score = string score
		def outcome = string(match.@outcome)
		params.outcome = outcome ?: matchScore.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.w_games = matchScore?.w_games
		params.l_games = matchScore?.l_games
		params.w_set_games = matchScore ? shortArray(conn, matchScore.w_set_games) : null
		params.l_set_games = matchScore ? shortArray(conn, matchScore.l_set_games) : null
		params.w_set_tb_pt = matchScore ? shortArray(conn, matchScore.w_set_tb_pt) : null
		params.l_set_tb_pt = matchScore ? shortArray(conn, matchScore.l_set_tb_pt) : null
		params.minutes = smallint match.@minutes
		setStatsParams(params, match.'winner-stats', 'w_')
		setStatsParams(params, match.'loser-stats', 'l_')
		return params
	}

	def setStatsParams(Map params, stats, prefix) {
		params[prefix + 'ace'] = stats?.ace
		params[prefix + 'df'] = stats?.df
		params[prefix + 'sv_pt'] = stats?.'sv-pt'
		params[prefix + '1st_in'] = stats?.'fst-in'
		params[prefix + '1st_won'] = stats?.'fst-won'
		params[prefix + '2nd_won'] = stats?.'snd-won'
		params[prefix + 'sv_gms'] = stats?.'sv-gms'
		params[prefix + 'bp_sv'] = stats?.'bp-sv'
		params[prefix + 'bp_fc'] = stats?.'bp-fc'
	}
}