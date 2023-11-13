package no.nav.tiltakspenger.utbetaling.exception.types

import no.nav.tiltakspenger.utbetaling.exception.BusinessException
import no.nav.tiltakspenger.utbetaling.exception.EntityNotFoundException
import no.nav.tiltakspenger.utbetaling.exception.MessageCode

class BehandlingIkkeFunnetException(behandlingId: String) :
    BusinessException(MessageCode.BEHANDLING_FINNES_IKKE, "")
    , EntityNotFoundException
