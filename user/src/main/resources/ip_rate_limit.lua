local key = KEYS[1]
local max_requests = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

local current = redis.call('GET', key)
if current == false then
    -- Key not in Redis. Set new key with ttl
    redis.call('SET', key, 1, 'EX', ttl)
    return { 1, ttl }
else
    -- Key found in Redis. Increment and check rate limit
    local count = tonumber(current)
    local remaining_ttl = redis.call('TTL', key)
    if count < max_requests then
        -- Rate limit not exceeded
        local new_count = redis.call('INCR', key)
        return { new_count, remaining_ttl }
    else
        -- Rate limit exceeded
        return { count + 1, remaining_ttl }
    end
end
