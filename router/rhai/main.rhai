const PROFUNDIDADE_MAXIMA = 10;

fn supergraph_service(service) {
    service.map_request(|request| {
        let id = if request.headers.contains("x-request-id") { request.headers["x-request-id"] } else { uuid_v4() };
        request.context["request_id"] = id;
        let query = request.body.query;
        if type_of(query) != "string" {
            return;
        }
        if !request.headers.contains("authorization") && !query.contains("__schema") {
            throw #{
                status: 401,
                message: "Header Authorization ausente. Gere um token com scripts/gerar-token.sh"
            };
        }
        let profundidade = profundidade_da_query(query);
        if !query.contains("__schema") && profundidade > PROFUNDIDADE_MAXIMA {
            throw #{
                status: 400,
                message: `Query com profundidade ${profundidade} excede o máximo de ${PROFUNDIDADE_MAXIMA}`
            };
        }
    });
    service.map_response(|response| {
        let id = response.context["request_id"];
        if id != () {
            response.headers["x-request-id"] = id;
        }
    });
}

fn subgraph_service(service, subgraph) {
    service.map_request(|request| {
        let id = request.context["request_id"];
        if id != () {
            request.subgraph.headers["x-request-id"] = id;
        }
    });
}

fn profundidade_da_query(query) {
    let atual = 0;
    let maxima = 0;
    let em_texto = false;
    for c in query {
        if c == '"' {
            em_texto = !em_texto;
        } else if !em_texto && c == '{' {
            atual += 1;
            if atual > maxima {
                maxima = atual;
            }
        } else if !em_texto && c == '}' {
            atual -= 1;
        }
    }
    maxima - 1
}
